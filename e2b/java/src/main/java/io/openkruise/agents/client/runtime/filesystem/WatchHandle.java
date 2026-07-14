package io.openkruise.agents.client.runtime.filesystem;

import io.openkruise.agents.client.runtime.utils.ConnectStreamReader;
import okhttp3.Response;

import java.io.Closeable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * WatchHandle represents a handle to a directory event stream being watched.
 * <p>
 * Stops watching and closes the underlying HTTP stream via {@link #stop()}.
 */
public class WatchHandle {
    private static final Logger LOG = Logger.getLogger(WatchHandle.class.getName());
    private final Closeable streamReader;
    private final Response streamingResponse;
    private volatile boolean stopped = false;

    public WatchHandle(Closeable streamReader, Response streamingResponse) {
        this.streamReader = streamReader;
        this.streamingResponse = streamingResponse;
    }

    /**
     * Stops watching the directory.
     */
    public void stop() {
        if (!stopped) {
            try {
                streamReader.close();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error closing watch stream reader", e);
            } finally {
                if (streamingResponse != null) {
                    try {
                        streamingResponse.close();
                    } catch (Exception ignored) {
                    }
                }
                stopped = true;
            }
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    public enum FilesystemEventType {
        CHMOD,
        CREATE,
        REMOVE,
        RENAME,
        WRITE
    }

    public static class FilesystemEvent {
        private final String name;
        private final FilesystemEventType type;

        public FilesystemEvent(String name, FilesystemEventType type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public FilesystemEventType getType() {
            return type;
        }

        @Override
        public String toString() {
            return String.format("FilesystemEvent{name='%s', type=%s}", name, type);
        }
    }
}
