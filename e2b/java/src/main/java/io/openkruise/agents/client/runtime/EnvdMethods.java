package io.openkruise.agents.client.runtime;

/**
 * Centralized constants for envd runtime service names and method names.
 * <p>
 * Avoids hardcoding strings in Commands / Filesystem modules for easier maintenance and debugging.
 */
public final class EnvdMethods {

    private EnvdMethods() {}

    // Process service

    /** Process service name */
    public static final String PROCESS_SERVICE = "process.Process";

    /** List all processes */
    public static final String PROCESS_LIST = "List";
    /** Start a process (Server-Streaming) */
    public static final String PROCESS_START = "Start";
    /** Connect to an existing process (Server-Streaming) */
    public static final String PROCESS_CONNECT = "Connect";
    /** Send standard input */
    public static final String PROCESS_SEND_INPUT = "SendInput";
    /** Send a signal */
    public static final String PROCESS_SEND_SIGNAL = "SendSignal";
    /** Close standard input */
    public static final String PROCESS_CLOSE_STDIN = "CloseStdin";

    // Filesystem service

    /** Filesystem service name */
    public static final String FILESYSTEM_SERVICE = "filesystem.Filesystem";

    /** List directory contents */
    public static final String FS_LIST_DIR = "ListDir";
    /** Create a directory */
    public static final String FS_MAKE_DIR = "MakeDir";
    /** Move/rename */
    public static final String FS_MOVE = "Move";
    /** Delete a file or directory */
    public static final String FS_REMOVE = "Remove";
    /** Get file/directory information */
    public static final String FS_STAT = "Stat";
    /** Watch directory changes (Server-Streaming) */
    public static final String FS_WATCH_DIR = "WatchDir";

    // File content read/write (HTTP route)

    /** HTTP route path for file content read/write */
    public static final String FILES_ROUTE = "/files";

    /** Default username for file read/write */
    public static final String DEFAULT_USERNAME = "root";
}
