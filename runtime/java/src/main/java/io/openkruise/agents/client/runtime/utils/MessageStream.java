package io.openkruise.agents.client.runtime.utils;

import java.io.Closeable;
import java.io.IOException;

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

    /**
     * Returns the last IO error swallowed by {@link #hasNext()} when it
     * returned false, or null if the stream ended cleanly. Lets callers
     * distinguish a truncated stream from a normal end. Default for
     * backward compatibility with existing implementors, matching
     * {@link #getTrailerError()}: an implementation that does not track
     * errors degrades gracefully — a truncated stream then surfaces as
     * "Command ended without an end event".
     */
    default IOException getLastError() {
        return null;
    }

    /**
     * Returns the server error from the Connect Protocol end-of-stream
     * trailer (available after {@link #hasNext()} returned false), or null
     * when the stream ended cleanly or the implementation has no trailer
     * support. Default for backward compatibility with existing
     * implementors.
     */
    default String getTrailerError() {
        return null;
    }
}
