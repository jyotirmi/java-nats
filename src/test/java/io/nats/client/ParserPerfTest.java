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

import static io.nats.client.UnitTestUtilities.newMockedConnection;

import org.junit.Test;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;

public class ParserPerfTest extends BaseUnitTest {
    @Test
    public void test() throws Exception {
        try (ConnectionImpl conn = (ConnectionImpl) newMockedConnection()) {
            final int BUF_SIZE = 65536;
            int count = 40000;

            Parser p = new Parser(conn);

            byte[] buf = new byte[BUF_SIZE];

            String msg = "MSG foo 1 4\r\ntest\r\n";
            byte[] msgBytes = msg.getBytes();
            int length = msgBytes.length;

            int bufLen = 0;
            int numMsgs = 0;
            for (int i = 0; (i + length) <= BUF_SIZE; i += length, numMsgs++) {
                System.arraycopy(msgBytes, 0, buf, i, length);
                bufLen += length;
            }

            System.err.printf("Parsing %d buffers of %d messages each (total=%d)\n", count, numMsgs,
                    count * numMsgs);

            long t0 = System.nanoTime();
            for (int i = 0; i < count; i++) {
                try {
                    p.parse(buf, bufLen);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    System.err.println("Error offset=" + e.getErrorOffset());
                    break;
                }
            }
            long elapsed = System.nanoTime() - t0;
            long avgNsec = elapsed / (count * numMsgs);
            long elapsedSec = TimeUnit.NANOSECONDS.toSeconds(elapsed);
            // long elapsedMsec = TimeUnit.NANOSECONDS.toMicros(elapsedNanos);

            long totalMsgs = numMsgs * count;
            System.err.printf("Parsed %d messages in %ds (%d msg/sec)\n", totalMsgs, elapsedSec,
                    (totalMsgs / elapsedSec));

            double totalBytes = (double) count * bufLen;
            double mbPerSec = totalBytes / elapsedSec / 1000000;
            System.err.printf("Parsed %.0fMB in %ds (%.0fMB/sec)\n", totalBytes / 1000000,
                    elapsedSec, mbPerSec);

            System.err.printf("Average parse time per msg = %dns\n", avgNsec);
        }
    }

    /**
     * Main executive.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        ParserPerfTest parserPerfTest = new ParserPerfTest();

        // b.testPubSpeed();
        parserPerfTest.test();
    }

}
