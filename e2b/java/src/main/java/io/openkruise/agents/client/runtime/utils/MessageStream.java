package io.openkruise.agents.client.runtime.utils;

import java.io.Closeable;

/**
 * A stream of typed messages with iterator-style access and lifecycle management.
 *
 * @param <T> message type
 */
public interface MessageStream<T> extends Closeable {

    /**
     * Returns true if there are more messages available in the stream.
     */
    boolean hasNext();

    /**
     * Returns the next message from the stream.
     *
     * @throws java.util.NoSuchElementException if no more messages are available
     */
    T next();
}