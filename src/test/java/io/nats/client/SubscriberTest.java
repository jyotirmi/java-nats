// Copyright 2015-2018 The NATS Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.nats.client;

import static io.nats.client.UnitTestUtilities.runDefaultServer;
import static io.nats.client.UnitTestUtilities.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import io.nats.examples.Publisher;
import io.nats.examples.Subscriber;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Category(IntegrationTest.class)
public class SubscriberTest extends BaseUnitTest  {

    final ExecutorService service = Executors.newCachedThreadPool();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testSubscriberStringArray() throws Exception {
        List<String> argList = new ArrayList<String>();
        argList.addAll(Arrays.asList("-s", Nats.DEFAULT_URL));
        argList.add("foo");

        String[] args = new String[argList.size()];
        args = argList.toArray(args);

        new Subscriber(args);
    }

    @Test
    public void testParseArgsBadFlags() {
        List<String> argList = new ArrayList<String>();
        String[] flags = new String[] {"-s", "--server", "-n", "--count", "-q", "--qgroup"};
        boolean exThrown = false;

        for (String flag : flags) {
            try {
                exThrown = false;
                argList.clear();
                argList.addAll(Arrays.asList(flag, "foo"));
                String[] args = new String[argList.size()];
                args = argList.toArray(args);
                new Subscriber(args);
            } catch (IllegalArgumentException e) {
                assertEquals(String.format("%s requires an argument", flag), e.getMessage());
                exThrown = true;
            } finally {
                assertTrue("Should have thrown exception", exThrown);
            }
        }
    }

    @Test
    public void testParseArgsNotEnoughArgs() {
        List<String> argList = new ArrayList<String>();
        boolean exThrown = false;

        try {
            exThrown = false;
            argList.clear();
            String[] args = new String[argList.size()];
            args = argList.toArray(args);
            new Subscriber(args);
        } catch (IllegalArgumentException e) {
            assertEquals("must supply at least a subject name", e.getMessage());
            exThrown = true;
        } finally {
            assertTrue("Should have thrown exception", exThrown);
        }
    }

    @Test(timeout = 5000)
    public void testMainSuccess() throws Exception {
        final List<Throwable> errors = new ArrayList<Throwable>();
        try (NatsServer srv = runDefaultServer()) {
            final CountDownLatch startPub = new CountDownLatch(1);
            final CountDownLatch done = new CountDownLatch(1);
            service.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Subscriber.main(new String[] {"-s", Nats.DEFAULT_URL, "-n", "1", "foo"});
                        done.countDown();
                    } catch (Exception e) {
                        errors.add(e);
                    }
                }
            });
            sleep(500);
            startPub.countDown();
            service.execute(new Runnable() {
                public void run() {
                    try {
                        startPub.await();
                        new Publisher(new String[] {"-s", Nats.DEFAULT_URL, "foo", "bar"}).run();
                    } catch (Exception e) {
                        errors.add(e);
                    }
                }
            });

            done.await();
            if (errors.size() != 0) {
                for (Throwable t : errors) {
                    t.printStackTrace();
                }
                fail("Unexpected exceptions");
            }
        }
    }

    @Test
    public void testMainFailsNoServers() throws Exception {
        thrown.expect(IOException.class);
        thrown.expectMessage(Nats.ERR_NO_SERVERS);
        Subscriber.main(new String[] {"-s", "nats://enterprise:4242", "foobar"});
    }
}
