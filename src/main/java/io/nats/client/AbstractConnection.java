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

import io.nats.client.Nats.ConnState;
import java.io.IOException;

/**
 * AbstractConnection is the base interface for all Connection variants.
 */
public interface AbstractConnection extends AutoCloseable {

    /**
     * Creates a {@link SyncSubscription} with interest in a given subject. In order to receive
     * messages, call one of the available {@link SyncSubscription#nextMessage()}.
     *
     * @param subject the subject of interest
     * @return the {@link SyncSubscription}
     * @throws IllegalArgumentException if the subject name contains illegal characters.
     * @throws NullPointerException     if the subject name is null
     * @throws IllegalStateException    if the connection is closed
     */
    SyncSubscription subscribe(String subject);

    /**
     * Creates a {@link SyncSubscription} with interest in a given subject. All subscribers with the
     * same queue name will form the queue group and only one member of the group will be selected
     * to receive any given message.
     *
     * @param subject the subject of interest
     * @param queue   the queue group
     * @return the {@code SyncSubscription}
     * @throws IllegalArgumentException if the subject (or queue) name contains illegal characters.
     * @throws NullPointerException     if the subject name is null
     * @throws IllegalStateException    if the connection is closed
     */
    SyncSubscription subscribe(String subject, String queue);

    /**
     * Creates a {@code AsyncSubscription} with interest in a given subject, assign the callback,
     * and immediately start receiving messages
     *
     * @param subject the subject of interest
     * @param cb      a {@code MessageHandler} object used to process messages received by the
     *                {@code AsyncSubscription}
     * @return the started {@code AsyncSubscription}
     * @throws IllegalArgumentException if the subject (or queue) name contains illegal characters.
     * @throws NullPointerException     if the subject name is null
     * @throws IllegalStateException    if the connection is closed
     */
    AsyncSubscription subscribe(String subject, MessageHandler cb);

    /**
     * Creates an asynchronous queue subscriber on a given subject of interest. All subscribers with
     * the same queue name will form the queue group and only one member of the group will be
     * selected to receive any given message asynchronously.
     *
     * @param subject the subject of interest
     * @param queue   the name of the queue group
     * @param cb      a {@code MessageHandler} object used to process messages received by the
     *                {@code Subscription}
     * @return {@code Subscription}
     */
    AsyncSubscription subscribe(String subject, String queue, MessageHandler cb);

    /**
     * Creates a {@code AsyncSubscription} with interest in a given subject, assign the callback,
     * and immediately start receiving messages
     *
     * @param subject the subject of interest
     * @param cb      a {@code MessageHandler} object used to process messages received by the
     *                {@code AsyncSubscription}
     * @return the started {@code AsyncSubscription}
     * @throws IllegalArgumentException if the subject (or queue) name contains illegal characters.
     * @throws NullPointerException     if the subject name is null
     * @throws IllegalStateException    if the connection is closed
     * @deprecated As of release 0.6, use {@link #subscribe(String, MessageHandler)} instead
     */
    AsyncSubscription subscribeAsync(String subject, MessageHandler cb);

    /**
     * Create an {@code AsyncSubscription} with interest in a given subject, assign the message
     * callback, and immediately start receiving messages.
     *
     * @param subject the subject of interest
     * @param queue   the name of the queue group
     * @param cb      a message callback for this subscription
     * @return the {@code AsyncSubscription}
     * @throws IllegalArgumentException if the subject (or queue) name contains illegal characters.
     * @throws NullPointerException     if the subject name is null
     * @throws IllegalStateException    if the connection is closed
     * @deprecated As of release 0.6, use {@link #subscribe(String, String, MessageHandler)} instead
     */
    AsyncSubscription subscribeAsync(String subject, String queue, MessageHandler cb);

    /**
     * Creates a synchronous queue subscriber on a given subject of interest. All subscribers with
     * the same queue name will form the queue group and only one member of the group will be
     * selected to receive any given message. {@code MessageHandler} must be registered, and
     * {@link AsyncSubscription#start()} must be called.
     *
     * @param subject the subject of interest
     * @param queue   the queue group
     * @return the {@code SyncSubscription}
     * @throws IllegalArgumentException if the subject (or queue) name contains illegal characters.
     * @throws NullPointerException     if the subject name is null
     * @throws IllegalStateException    if the connection is closed
     */
    SyncSubscription subscribeSync(String subject, String queue);

    /**
     * Creates a {@link SyncSubscription} with interest in a given subject. In order to receive
     * messages, call one of the available {@link SyncSubscription#nextMessage()}.
     *
     * @param subject the subject of interest
     * @return the {@link SyncSubscription}
     * @throws IllegalArgumentException if the subject name contains illegal characters.
     * @throws NullPointerException     if the subject name is null
     * @throws IllegalStateException    if the connection is closed
     */
    SyncSubscription subscribeSync(String subject);

    /**
     * Creates a new, uniquely named inbox with the prefix '_INBOX.'
     *
     * @return the newly created inbox subject
     */
    String newInbox();

    /**
     * Closes the connection, also closing all subscriptions on this connection.
     *
     * <p>When {@code close()} is called, the following things happen, in order: <ol> <li>The
     * Connection is flushed, and any other pending flushes previously requested by the user are
     * immediately cleared. <li>Message delivery to all active subscriptions is terminated
     * immediately, without regard to any messages that may have been delivered to the client's
     * connection, but not to the relevant subscription(s). Any such undelivered messages are
     * discarded immediately. <li>The DisconnectedCallback, if registered, is invoked. <li>The
     * ClosedCallback, if registered, is invoked. <li>The TCP/IP socket connection to the NATS
     * server is gracefully closed. </ol>
     *
     * @see java.lang.AutoCloseable#close()
     */
    void close();

    /**
     * Indicates whether the connection has been closed.
     *
     * @return {@code true} if the connection is closed, otherwise {@code false}
     */
    boolean isClosed();

    /**
     * Indicates whether the connection is currently connected.
     *
     * @return {@code true} if the connection is currently connected, otherwise {@code false}
     */
    boolean isConnected();

    /**
     * Indicates whether the connection is currently reconnecting.
     *
     * @return {@code true} if the connection is currently reconnecting, otherwise {@code false}
     */
    boolean isReconnecting();

    /**
     * Indicates whether the connected server requires authorization.
     *
     * @return {@code true} if the connected server requires authorization, otherwise {@code false}
     */
    boolean isAuthRequired();

    /**
     * Indicates whether the connected server requires TLS connections.
     *
     * @return {@code true} if the connected server requires TLS connections, otherwise
     * {@code false}
     */
    boolean isTlsRequired();

    /**
     * Retrieves the connection statistics.
     *
     * @return the statistics for this connection.
     * @see Statistics
     */
    Statistics getStats();

    /**
     * Resets the gathered statistics for this connection.
     *
     * @see Statistics
     */
    void resetStats();

    /**
     * Gets the maximum payload size this connection will accept.
     *
     * @return the maximum message payload size in bytes.
     */
    long getMaxPayload();

    /**
     * Flushes the current connection, waiting up to {@code timeout} for successful completion.
     *
     * @param timeout the connection timeout in milliseconds.
     * @throws IOException if a connection-related error prevents the flush from completing
     * @throws InterruptedException if the calling thread is interrupted before the flush completes
     */
    void flush(int timeout) throws IOException, InterruptedException;

    /**
     * Flushes the current connection, waiting up to 60 seconds for completion.
     *
     * @throws IOException if a connection-related issue prevented the flush from completing
     *                     successfully
     * @throws InterruptedException if the calling thread is interrupted before the flush completes
     * @see #flush(int)
     */
    void flush() throws IOException, InterruptedException;

    /**
     * Returns the connection's asynchronous exception callback.
     *
     * @return the asynchronous exception handler for this connection
     * @see ExceptionHandler
     */
    ExceptionHandler getExceptionHandler();

    /**
     * Sets the connection's asynchronous exception callback.
     *
     * @param exceptionHandler the asynchronous exception handler to set for this connection
     * @see ExceptionHandler
     */
    void setExceptionHandler(ExceptionHandler exceptionHandler);

    /**
     * Returns the connection closed callback.
     *
     * @return the connection closed callback for this connection
     * @see ClosedCallback
     */
    ClosedCallback getClosedCallback();

    /**
     * Sets the connection closed callback.
     *
     * @param cb the connection closed callback to set
     */
    void setClosedCallback(ClosedCallback cb);

    /**
     * Returns the connection disconnected callback.
     *
     * @return the disconnected callback for this connection
     */
    DisconnectedCallback getDisconnectedCallback();

    /**
     * Sets the connection disconnected callback.
     *
     * @param cb the disconnected callback to set
     */
    void setDisconnectedCallback(DisconnectedCallback cb);

    /**
     * Returns the connection reconnected callback.
     *
     * @return the reconnect callback for this connection
     */
    ReconnectedCallback getReconnectedCallback();

    /**
     * Sets the connection reconnected callback.
     *
     * @param cb the reconnect callback to set for this connection
     */
    void setReconnectedCallback(ReconnectedCallback cb);

    /**
     * Returns the URL string of the currently connected NATS server.
     *
     * @return the URL string of the currently connected NATS server.
     */
    String getConnectedUrl();

    /**
     * Returns the unique server ID string of the connected server.
     *
     * @return the unique server ID string of the connected server
     */
    String getConnectedServerId();

    /**
     * Returns the list of known server URLs, including URLs that have been discovered since the
     * connection was established.
     *
     * @return the list of known server URLs
     */
    String[] getServers();

    /**
     * Returns the server URLs that have been discovered since the connection was established.
     *
     * @return the server URLs that have been discovered since the connection has been established
     */
    String[] getDiscoveredServers();

    /**
     * Returns the current connection state.
     *
     * @return the current connection state
     */
    ConnState getState();

    /**
     * Returns the details from the INFO protocol message received.
     *
     * @return the details from the INFO protocol message received from the NATS server on initial
     * establishment of a TCP connection.
     * @see ServerInfo
     */
    ServerInfo getConnectedServerInfo();

    /**
     * Returns the last exception registered on the connection.
     *
     * @return the last exception registered on this connection
     */
    Exception getLastException();

    /**
     * Returns the name of this Connection.
     *
     * @return the name of this Connection
     */
    String getName();

    /**
     * Returns the number of valid bytes in the pending output buffer. This buffer is only used
     * during disconnect/reconnect sequences to buffer messages that are published during a
     * temporary disconnection.
     *
     * @return the number of valid bytes in the pending output buffer.
     */
    int getPendingByteCount();

}
