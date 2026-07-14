package io.openkruise.agents.client.runtime.filesystem;

import io.grpc.stub.StreamObserver;
import io.openkruise.agents.client.runtime.envd.filesystem.EventType;
import io.openkruise.agents.client.runtime.envd.filesystem.FilesystemEvent;
import io.openkruise.agents.client.runtime.envd.filesystem.WatchDirResponse;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Observer for handling WatchDirResponse stream events.
 * Converts protobuf WatchDirResponse to WatchHandle.FilesystemEvent and notifies the consumer.
 */
public class WatchDirResponseObserver implements StreamObserver<WatchDirResponse> {
    private static final Logger LOG = Logger.getLogger(WatchDirResponseObserver.class.getName());
    private final Consumer<WatchHandle.FilesystemEvent> onEvent;

    public WatchDirResponseObserver(Consumer<WatchHandle.FilesystemEvent> onEvent) {
        this.onEvent = onEvent;
    }

    @Override
    public void onNext(WatchDirResponse response) {
        // Process the response - check if it's a filesystem event
        // Note: WatchDirResponse uses oneof field, so we need to check the event case
        if (response.getEventCase() == WatchDirResponse.EventCase.FILESYSTEM) {
            FilesystemEvent fsEvent = response.getFilesystem();

            WatchHandle.FilesystemEventType eventType = mapEventType(fsEvent.getType());
            if (eventType != null) {
                WatchHandle.FilesystemEvent event = new WatchHandle.FilesystemEvent(
                    fsEvent.getName(),
                    eventType
                );

                onEvent.accept(event);
            }
        }
    }

    @Override
    public void onError(Throwable t) {
        LOG.log(Level.WARNING, "Error in WatchDirResponseObserver", t);
    }

    @Override
    public void onCompleted() {
        // Empty implementation - stream completed
    }

    /**
     * Map protobuf EventType to WatchHandle.FilesystemEventType.
     * 
     * @param eventType protobuf EventType enum
     * @return corresponding WatchHandle.FilesystemEventType, or null if unmapped
     */
    private WatchHandle.FilesystemEventType mapEventType(EventType eventType) {
        switch (eventType) {
            case EVENT_TYPE_CHMOD:
                return WatchHandle.FilesystemEventType.CHMOD;
            case EVENT_TYPE_CREATE:
                return WatchHandle.FilesystemEventType.CREATE;
            case EVENT_TYPE_REMOVE:
                return WatchHandle.FilesystemEventType.REMOVE;
            case EVENT_TYPE_RENAME:
                return WatchHandle.FilesystemEventType.RENAME;
            case EVENT_TYPE_WRITE:
                return WatchHandle.FilesystemEventType.WRITE;
            default:
                return null;
        }
    }
}
