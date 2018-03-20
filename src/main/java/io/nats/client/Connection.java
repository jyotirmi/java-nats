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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A {@code Connection} object is a client's active connection to a NATS server.
 *
 * <p>To connect to a NATS server using the default URL:
 *
 * <pre>
 *     Connection nc = Nats.connect();
 * </pre>
 *
 * <p>To connect to a NATS server using a custom URL:
 *
 * <pre>
 *     Connection nc = Nats.connect("nats://someserver:4222");
 * </pre>
 *
 * <p>To connect to a NATS server using a custom URL list:
 * <pre>
 *     Connection nc = Nats.connect("nats://someserver:4222, nats://anotherserver:4222");
 * </pre>
 *
 * <p>To connect to a NATS server using a custom URL and other custom options:
 * <pre>
 *     Connection nc = Nats.connect("nats://someserver:4222", new Options.Builder()
 *             .dontRandomize()
 *             .maxReconnect(1)
 *             .reconnectWait(200)
 *             .build());
 * </pre>
 */
public interface Connection extends AbstractConnection {
    /**
     * Publishes the payload specified by {@code data} to the subject specified by {@code subject}.
     *
     * @param subject the subject to publish the message to
     * @param data    the message payload
     * @throws IOException if an I/O error is encountered
     */
    void publish(String subject, byte[] data) throws IOException;

    /**
     * Publishes a message to a subject. The subject is set via {@link Message#setSubject(String)}
     * or the {@link Message#Message(String, String, byte[])} constructor.
     *
     * @param msg the {@code Message} to publish
     * @throws IOException if an I/O error is encountered
     */
    void publish(Message msg) throws IOException;

    /**
     * Publishes the payload specified by {@code data} to the subject specified by {@code subject},
     * with an optional reply subject. If {@code reply} is {@code null}, the behavior is identical
     * to {@link #publish(String, byte[])}
     *
     * @param subject the subject to publish the message to
     * @param reply   the subject to which subscribers should send responses
     * @param data    the message payload
     * @throws IOException if an I/O error is encountered
     */
    void publish(String subject, String reply, byte[] data) throws IOException;

    /**
     * Publishes the payload specified by {@code data} to the subject specified by {@code subject},
     * with an optional reply subject. If {@code reply} is {@code null}, the behavior is identical
     * to {@link #publish(String, byte[])}. if {@code flush} is {@code true}, a flush of the
     * Connection's output stream will be forced.
     *
     * @param subject the subject to publish the message to
     * @param reply   the subject to which subscribers should send responses
     * @param data    the message payload
     * @param flush   whether to force a flush of the output stream
     * @throws IOException if an I/O error is encountered
     */
    void publish(String subject, String reply, byte[] data, boolean flush) throws IOException;

    /**
     * Publishes a request message to the specified subject, waiting up to {@code timeout} msec for
     * a response.
     *
     * @param subject the subject to publish the request message to
     * @param data    the request message payload
     * @param timeout how long to wait for a response message (in msec)
     * @return the response message, or {@code null} if timed out
     * @throws IOException          if a connection-related error occurs
     * @throws InterruptedException if {@link Thread#interrupt() interrupted} while waiting to
     *                              receive a response
     */
    Message request(String subject, byte[] data, long timeout)
            throws IOException, InterruptedException;

    /**
     * Publishes a request message to the specified subject, waiting up to {@code timeout} msec for
     * a response.
     *
     * @param subject the subject to publish the request message to
     * @param data    the request message payload
     * @param timeout how long to wait for a response message (in msec)
     * @param unit    how long to wait for a response message (in msec)
     * @return the response message, or {@code null} if timed out
     * @throws IOException          if a connection-related error occurs
     * @throws InterruptedException if {@link Thread#interrupt() interrupted} while waiting to
     *                              receive a response
     */
    Message request(String subject, byte[] data, long timeout, TimeUnit unit)
            throws IOException, InterruptedException;

    /**
     * Publishes a request message to the specified subject, waiting for a response until one is
     * available.
     *
     * @param subject the subject to publish the request message to
     * @param data    the request message payload
     * @return the response message, or {@code null} if timed out
     * @throws IOException          if a connection-related error occurs
     * @throws InterruptedException if {@link Thread#interrupt() interrupted} while waiting to
     *                              receive a response
     */
    Message request(String subject, byte[] data) throws IOException, InterruptedException;
}
