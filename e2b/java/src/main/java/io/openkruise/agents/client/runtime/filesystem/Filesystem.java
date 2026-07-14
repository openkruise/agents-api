package io.openkruise.agents.client.runtime.filesystem;

import com.google.protobuf.util.JsonFormat;
import io.openkruise.agents.client.runtime.EnvdMethods;
import io.openkruise.agents.client.runtime.RuntimeConfig;
import io.openkruise.agents.client.runtime.envd.filesystem.*;
import io.openkruise.agents.client.runtime.exceptions.SandboxException;
import io.openkruise.agents.client.runtime.utils.ConnectStreamReader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Filesystem provides filesystem operation functionality within the sandbox.
 * <p>
 * All implemented based on OkHttp + Connect Protocol, without depending on gRPC.
 */
public class Filesystem {
    private static final Logger LOG = Logger.getLogger(Filesystem.class.getName());
    private static final JsonFormat.Parser PROTO_PARSER = JsonFormat.parser().ignoringUnknownFields();
    private final String sandboxID;
    private final RuntimeConfig config;
    private final OkHttpClient httpClient;
    private final OkHttpClient streamingClient;
    /** Tracks active WatchHandles so they can be closed when the client shuts down (M5). */
    private final Set<WatchHandle> activeWatchHandles =
        ConcurrentHashMap.newKeySet();

    public Filesystem(String sandboxID, RuntimeConfig config, OkHttpClient httpClient, OkHttpClient streamingClient) {
        this.sandboxID = Objects.requireNonNull(sandboxID, "sandboxID cannot be null");
        this.config = Objects.requireNonNull(config, "RuntimeConfig cannot be null");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient cannot be null");
        this.streamingClient = Objects.requireNonNull(streamingClient, "streamingClient cannot be null");
    }

    /**
     * List entries in a directory.
     *
     * @param path path to the directory
     * @return list of entries in the directory
     */
    public List<EntryInfo> listDir(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }
        return listDir(path, 1);
    }

    /**
     * List entries in a directory with specified depth.
     *
     * @param path  path to the directory
     * @param depth depth of the directory to list
     * @return list of entries in the directory
     */
    public List<EntryInfo> listDir(String path, int depth) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }
        if (depth < 1) {
            throw new IllegalArgumentException("Depth must be at least 1");
        }

        ListDirRequest params = ListDirRequest.newBuilder().setPath(path).setDepth(depth).build();
        Request request = config.buildHttpRequest(EnvdMethods.FILESYSTEM_SERVICE, EnvdMethods.FS_LIST_DIR, params, sandboxID);

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("ListDir HTTP request failed: " + response.code() + " " + response.message());
            }

            Reader reader = response.body().charStream();
            ListDirResponse.Builder builder = ListDirResponse.newBuilder();
            PROTO_PARSER.merge(reader, builder);
            ListDirResponse listDirResponse = builder.build();

            List<EntryInfo> entries = new ArrayList<>(listDirResponse.getEntriesCount());
            for (io.openkruise.agents.client.runtime.envd.filesystem.EntryInfo entry :
                listDirResponse.getEntriesList()) {
                entries.add(toEntryInfo(entry));
            }
            return entries;
        } catch (Exception e) {
            throw new RuntimeException("Failed to listDir", e);
        }
    }

    /**
     * Create a new directory and all directories along the way if needed on the specified path.
     *
     * @param path path to a new directory
     * @return true if the directory was created, false if it already exists
     */
    public boolean makeDir(String path) {
        MakeDirRequest params = MakeDirRequest.newBuilder()
            .setPath(path)
            .build();

        Request request = config.buildHttpRequest(EnvdMethods.FILESYSTEM_SERVICE, EnvdMethods.FS_MAKE_DIR, params, sandboxID);

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("MakeDir HTTP request failed: " + response.code() + " " + response.message());
            }
            return response.isSuccessful();
        } catch (Exception e) {
            throw new RuntimeException("Failed to makeDir", e);
        }
    }

    /**
     * Rename a file or directory.
     *
     * @param oldPath path to the file or directory to rename
     * @param newPath new path for the file or directory
     * @return information about renamed file or directory
     */
    public boolean move(String oldPath, String newPath) {
        MoveRequest params = MoveRequest.newBuilder()
            .setSource(oldPath)
            .setDestination(newPath)
            .build();

        Request request = config.buildHttpRequest(EnvdMethods.FILESYSTEM_SERVICE, EnvdMethods.FS_MOVE, params, sandboxID);

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Move HTTP request failed: " + response.code() + " " + response.message());
            }
            return response.isSuccessful();
        } catch (Exception e) {
            throw new RuntimeException("Failed to move", e);
        }
    }

    /**
     * Remove a file or directory.
     *
     * @param path path to a file or directory
     */
    public boolean remove(String path) {
        RemoveRequest params = RemoveRequest.newBuilder()
            .setPath(path)
            .build();

        Request request = config.buildHttpRequest(EnvdMethods.FILESYSTEM_SERVICE, EnvdMethods.FS_REMOVE, params, sandboxID);

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Remove HTTP request failed: " + response.code() + " " + response.message());
            }
            return response.isSuccessful();
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove", e);
        }
    }

    /**
     * Check if a file or a directory exists.
     *
     * @param path path to a file or a directory
     * @return true if the file or directory exists, false otherwise
     */
    public boolean exists(String path) {
        StatRequest params = StatRequest.newBuilder()
            .setPath(path)
            .build();

        Request request = config.buildHttpRequest(EnvdMethods.FILESYSTEM_SERVICE, EnvdMethods.FS_STAT, params, sandboxID);

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                return false;
            }
            if (!response.isSuccessful()) {
                throw new IOException("Stat HTTP request failed: " + response.code() + " " + response.message());
            }
            return response.isSuccessful();
        } catch (Exception e) {
            throw new RuntimeException("Failed to check if file exists: " + path, e);
        }
    }

    /**
     * Get information about a file or directory.
     *
     * @param path path to a file or directory
     * @return information about the file or directory
     */
    public EntryInfo getInfo(String path) {
        StatRequest params = StatRequest.newBuilder()
            .setPath(path)
            .build();

        Request request = config.buildHttpRequest(EnvdMethods.FILESYSTEM_SERVICE, EnvdMethods.FS_STAT, params, sandboxID);

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Stat HTTP request failed: " + response.code() + " " + response.message());
            }

            Reader reader = response.body().charStream();
            StatResponse.Builder builder = StatResponse.newBuilder();
            PROTO_PARSER.merge(reader, builder);
            StatResponse statResponse = builder.build();

            return toEntryInfo(statResponse.getEntry());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get file info: " + path, e);
        }
    }

    /**
     * Reads file content and returns byte array.
     *
     * @param path file path
     * @return file content
     */
    public byte[] read(String path) {
        return read(path, EnvdMethods.DEFAULT_USERNAME);
    }

    /**
     * Reads file content and returns byte array.
     *
     * @param path file path
     * @param user username executing the operation
     * @return file content
     */
    public byte[] read(String path, String user) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }
        if (user == null || user.isEmpty()) {
            user = EnvdMethods.DEFAULT_USERNAME;
        }

        String baseUrl = config.getSandboxURL(sandboxID);
        String fileUrl = String.format("%s%s?path=%s&username=%s", baseUrl, EnvdMethods.FILES_ROUTE,
            urlEncode(path), urlEncode(user));

        Request request = new Request.Builder()
            .url(fileUrl)
            .get()
            .build();
        request = addSandboxHeaders(request);

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new RuntimeException("File not found: " + path);
            }
            if (!response.isSuccessful()) {
                String body = response.body() != null ? response.body().string() : "";
                throw new IOException("Read file failed (status " + response.code() + "): " + body);
            }
            return response.body().bytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + path, e);
        }
    }

    /**
     * Reads file content and returns string (UTF-8).
     *
     * @param path file path
     * @return file content as string
     */
    public String readText(String path) {
        return new String(read(path), StandardCharsets.UTF_8);
    }

    /**
     * Reads file content and returns string (UTF-8).
     *
     * @param path file path
     * @param user username executing the operation
     * @return file content as string
     */
    public String readText(String path, String user) {
        return new String(read(path, user), StandardCharsets.UTF_8);
    }

    /**
     * Writes file content. Creates the file if it doesn't exist, overwrites if it does.
     * Parent directories are created automatically if they don't exist.
     *
     * @param path file path
     * @param data file content
     * @return write result information
     */
    public WriteInfo write(String path, byte[] data) {
        return write(path, data, EnvdMethods.DEFAULT_USERNAME);
    }

    /**
     * Writes file content. Creates the file if it doesn't exist, overwrites if it does.
     *
     * @param path file path
     * @param data file content
     * @param user username executing the operation
     * @return write result information
     */
    public WriteInfo write(String path, byte[] data, String user) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        if (user == null || user.isEmpty()) {
            user = EnvdMethods.DEFAULT_USERNAME;
        }

        String baseUrl = config.getSandboxURL(sandboxID);
        String fileUrl = String.format("%s%s?path=%s&username=%s", baseUrl, EnvdMethods.FILES_ROUTE,
            urlEncode(path), urlEncode(user));

        // Build multipart/form-data request body
        okhttp3.MultipartBody requestBody = new okhttp3.MultipartBody.Builder()
            .setType(okhttp3.MultipartBody.FORM)
            .addFormDataPart("file", path,
                okhttp3.RequestBody.create(data, okhttp3.MediaType.get("application/octet-stream")))
            .build();

        Request request = new Request.Builder()
            .url(fileUrl)
            .post(requestBody)
            .build();
        request = addSandboxHeaders(request);

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.code() != HttpURLConnection.HTTP_OK
                && response.code() != HttpURLConnection.HTTP_CREATED) {
                String body = response.body() != null ? response.body().string() : "";
                throw new IOException("Write file failed (status " + response.code() + "): " + body);
            }

            // Try to parse response
            String respBody = response.body() != null ? response.body().string() : "";
            if (!respBody.isEmpty()) {
                try {
                    com.google.gson.JsonArray arr = com.google.gson.JsonParser.parseString(respBody).getAsJsonArray();
                    if (arr.size() > 0) {
                        com.google.gson.JsonObject obj = arr.get(0).getAsJsonObject();
                        return new WriteInfo(
                            obj.has("path") ? obj.get("path").getAsString() : path,
                            obj.has("type") ? obj.get("type").getAsString() : "file");
                    }
                } catch (Exception e) {
                    LOG.log(Level.FINE, "Failed to parse write response for " + path + ", falling back to basic info", e);
                }
            }
            return new WriteInfo(path, "file");
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + path, e);
        }
    }

    /**
     * Writes text content to file (UTF-8).
     *
     * @param path    file path
     * @param content text content
     * @return write result information
     */
    public WriteInfo writeText(String path, String content) {
        return write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Writes text content to file (UTF-8).
     *
     * @param path    file path
     * @param content text content
     * @param user    username executing the operation
     * @return write result information
     */
    public WriteInfo writeText(String path, String content, String user) {
        return write(path, content.getBytes(StandardCharsets.UTF_8), user);
    }

    // ======================== Directory watching ========================

    /**
     * Start watching a directory for filesystem events.
     *
     * @param path    path to directory to watch
     * @param onEvent callback to call when an event in the directory occurs
     * @return WatchHandle object for stopping watching directory
     */
    public WatchHandle watchDir(String path, Consumer<WatchHandle.FilesystemEvent> onEvent) {
        return watchDir(path, false, onEvent);
    }

    /**
     * Start watching a directory for filesystem events.
     *
     * @param path      path to directory to watch
     * @param recursive whether to watch subdirectories recursively
     * @param onEvent   callback to call when an event in the directory occurs
     * @return WatchHandle object for stopping watching directory
     */
    public WatchHandle watchDir(String path, boolean recursive, Consumer<WatchHandle.FilesystemEvent> onEvent) {
        WatchDirRequest watchReq = WatchDirRequest.newBuilder().setPath(path).setRecursive(recursive).build();

        // Initiate request via Connect Protocol Server-Streaming
        Request httpRequest = config.buildStreamingRequest(EnvdMethods.FILESYSTEM_SERVICE, EnvdMethods.FS_WATCH_DIR, watchReq, sandboxID);
        Response response;
        try {
            // noinspection WithSSRFCheckingInspection
            response = streamingClient.newCall(httpRequest).execute();
        } catch (IOException e) {
            throw new SandboxException("Failed to watch directory", e);
        }
        if (!response.isSuccessful()) {
            String msg = "WatchDir HTTP request failed: " + response.code() + " " + response.message();
            response.close();
            throw new SandboxException(msg);
        }

        InputStream responseStream = response.body().byteStream();
        ConnectStreamReader<WatchDirResponse> streamReader =
            new ConnectStreamReader<>(responseStream, WatchDirResponse.parser());

        // Start background thread to read event stream
        Thread watchThread = new Thread(() -> {
            try {
                while (streamReader.hasNext()) {
                    WatchDirResponse watchResponse = streamReader.next();
                    // WatchDirResponse is oneof event { StartEvent, FilesystemEvent, KeepAlive }
                    if (watchResponse.hasFilesystem()) {
                        FilesystemEvent fsEvent = watchResponse.getFilesystem();
                        WatchHandle.FilesystemEvent event = new WatchHandle.FilesystemEvent(
                            fsEvent.getName(),
                            mapEventType(fsEvent.getType()));
                        if (onEvent != null) {
                            onEvent.accept(event);
                        }
                    }
                    // Ignore start and keepalive events
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Watch directory stream error for path: " + path, e);
            } finally {
                streamReader.close();
            }
        }, "watchDir-" + path);
        watchThread.setDaemon(true);
        watchThread.start();

        WatchHandle handle = new WatchHandle(streamReader, response);
        activeWatchHandles.add(handle);
        return handle;
    }

    /**
     * Stops all active directory watches and clears the tracking set.
     * Called by {@link io.openkruise.agents.client.runtime.RuntimeClient#close()} before config shutdown.
     */
    public void closeAllWatchHandles() {
        for (WatchHandle handle : activeWatchHandles) {
            try {
                handle.stop();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error stopping watch handle during shutdown", e);
            }
        }
        activeWatchHandles.clear();
    }

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
                return WatchHandle.FilesystemEventType.WRITE;
        }
    }

    private EntryInfo toEntryInfo(io.openkruise.agents.client.runtime.envd.filesystem.EntryInfo entry) {
        return new EntryInfo(entry.getName(), mapFileType(entry.getType()), entry.getPath(), entry.getSize(),
            entry.getMode(), entry.getPermissions(), entry.getOwner(), entry.getGroup(),
            entry.hasModifiedTime() ? entry.getModifiedTime() : null,
            entry.hasSymlinkTarget() ? entry.getSymlinkTarget() : null);
    }

    private FileType mapFileType(io.openkruise.agents.client.runtime.envd.filesystem.FileType fileType) {
        switch (fileType) {
            case FILE_TYPE_DIRECTORY:
                return FileType.DIR;
            case FILE_TYPE_FILE:
                return FileType.FILE;
            case FILE_TYPE_SYMLINK:
                return FileType.SYMLINK;
            default:
                throw new SandboxException("Unknown file type: " + fileType);
        }
    }

    public enum FileType {
        FILE,
        DIR,
        SYMLINK
    }

    public static class EntryInfo {
        private final String name;
        private final FileType type;
        private final String path;
        private final long size;
        private final int mode;
        private final String permissions;
        private final String owner;
        private final String group;
        private final com.google.protobuf.Timestamp modifiedTime; // Use correct Timestamp type
        private final String symlinkTarget;

        public EntryInfo(String name, FileType type, String path, long size, int mode, String permissions, String owner,
            String group, com.google.protobuf.Timestamp modifiedTime, String symlinkTarget) {
            this.name = name;
            this.type = type;
            this.path = path;
            this.size = size;
            this.mode = mode;
            this.permissions = permissions;
            this.owner = owner;
            this.group = group;
            this.modifiedTime = modifiedTime;
            this.symlinkTarget = symlinkTarget;
        }

        // Getters
        public String getName() {return name;}

        public FileType getType() {return type;}

        public String getPath() {return path;}

        public long getSize() {return size;}

        public int getMode() {return mode;}

        public String getPermissions() {return permissions;}

        public String getOwner() {return owner;}

        public String getGroup() {return group;}

        public com.google.protobuf.Timestamp getModifiedTime() {return modifiedTime;} // More accurate type

        public String getSymlinkTarget() {return symlinkTarget;}

        @Override
        public String toString() {
            return String.format("EntryInfo{name='%s', type=%s, path='%s', size=%d}", name, type, path, size);
        }
    }

    /**
     * File write result information.
     */
    public static class WriteInfo {
        private final String path;
        private final String type;

        public WriteInfo(String path, String type) {
            this.path = path;
            this.type = type;
        }

        public String getPath() {return path;}

        public String getType() {return type;}

        @Override
        public String toString() {
            return "WriteInfo{path='" + path + "', type='" + type + "'}";
        }
    }

    // ======================== Internal helper methods ========================

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private Request addSandboxHeaders(Request original) {
        Map<String, String> hdrs = config.getSandboxHeaders(sandboxID);
        Request.Builder builder = original.newBuilder();
        for (Map.Entry<String, String> entry : hdrs.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }
}
