package io.openkruise.agents.client.runtime.commands;

import com.google.protobuf.util.JsonFormat;
import io.openkruise.agents.client.runtime.EnvdMethods;
import io.openkruise.agents.client.runtime.RuntimeConfig;
import io.openkruise.agents.client.runtime.envd.process.*;
import io.openkruise.agents.client.runtime.exceptions.SandboxException;
import io.openkruise.agents.client.runtime.utils.ConnectStreamReader;
import io.openkruise.agents.client.runtime.utils.MessageStream;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Commands provides command execution functionality within the sandbox.
 * <p>
 * All implemented based on OkHttp + Connect Protocol, without depending on gRPC.
 * <ul>
 *   <li>Unary calls (List, SendInput, SendSignal, CloseStdin): HTTP/JSON POST</li>
 *   <li>Streaming calls (Start, Connect): Connect Protocol Server-Streaming (protobuf frames)</li>
 * </ul>
 */
public class Commands {
    private static final JsonFormat.Parser PROTO_PARSER = JsonFormat.parser().ignoringUnknownFields();
    private final String sandboxID;
    private final RuntimeConfig config;
    private final OkHttpClient httpClient;
    private final OkHttpClient streamingClient;

    public Commands(String sandboxID, RuntimeConfig config, OkHttpClient httpClient, OkHttpClient streamingClient) {
        this.sandboxID = Objects.requireNonNull(sandboxID, "sandboxID cannot be null");
        this.config = Objects.requireNonNull(config, "RuntimeConfig cannot be null");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient cannot be null");
        this.streamingClient = Objects.requireNonNull(streamingClient, "streamingClient cannot be null");
    }

    public List<ProcessInfo> list() {
        ListRequest params = ListRequest.newBuilder().build();
        Request request = config.buildHttpRequest(EnvdMethods.PROCESS_SERVICE, EnvdMethods.PROCESS_LIST, params, sandboxID);

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("List HTTP request failed: " + response.code() + " " + response.message());
            }

            Reader reader = response.body().charStream();
            ListResponse.Builder builder = ListResponse.newBuilder();
            PROTO_PARSER.merge(reader, builder);
            ListResponse listResponse = builder.build();

            List<ProcessInfo> entries = new ArrayList<>(listResponse.getProcessesCount());
            for (io.openkruise.agents.client.runtime.envd.process.ProcessInfo entry :
                listResponse.getProcessesList()) {
                entries.add(toProcessInfo(entry));
            }
            return entries;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list", e);
        }
    }

    private ProcessInfo toProcessInfo(io.openkruise.agents.client.runtime.envd.process.ProcessInfo p) {
        return new ProcessInfo(
            p.getPid(),
            p.hasTag() ? p.getTag() : null,
            p.getConfig().getCmd(),
            p.getConfig().getArgsList(),
            p.getConfig().getEnvsMap(),
            p.getConfig().hasCwd() ? p.getConfig().getCwd() : null
        );
    }

    public CommandResult run(String cmd) {
        return run(cmd, new RunOptions());
    }

    public CommandResult run(String cmd, RunOptions options) {
        CommandHandle handle = null;
        try {
            handle = runBackground(cmd, options);
            return handle.waitForCompletion();
        } finally {
            if (handle != null) {
                handle.close();
            }
        }
    }

    public CommandHandle runBackground(String cmd, RunOptions options) {
        if (cmd == null || cmd.trim().isEmpty()) {
            throw new IllegalArgumentException("Command cannot be null or empty");
        }
        if (options == null) {
            options = new RunOptions();
        }

        String shell = (options.getShell() != null && !options.getShell().isEmpty())
            ? options.getShell() : "/bin/bash";

        ProcessConfig.Builder processConfig = ProcessConfig.newBuilder()
            .setCmd(shell)
            .addArgs("-l")
            .addArgs("-c")
            .addArgs(cmd);

        if (options.getCwd() != null) {
            processConfig.setCwd(options.getCwd());
        }

        if (options.getEnvs() != null) {
            processConfig.putAllEnvs(options.getEnvs());
        }

        StartRequest startReq = StartRequest.newBuilder()
            .setProcess(processConfig)
            .setStdin(options.isStdin())
            .build();

        // Initiate request via Connect Protocol Server-Streaming
        Request httpRequest = config.buildStreamingRequest(EnvdMethods.PROCESS_SERVICE, EnvdMethods.PROCESS_START, startReq, sandboxID);
        Response response;
        try {
            // noinspection WithSSRFCheckingInspection
            response = streamingClient.newCall(httpRequest).execute();
        } catch (IOException e) {
            throw new SandboxException("Failed to start process", e);
        }
        if (!response.isSuccessful()) {
            String msg = "Start HTTP request failed: " + response.code() + " " + response.message();
            response.close();
            throw new SandboxException(msg);
        }

        // Parse Connect Protocol frames from response stream
        InputStream responseStream = response.body().byteStream();
        ConnectStreamReader<StartResponse> streamReader =
            new ConnectStreamReader<>(responseStream, StartResponse.parser());

        if (!streamReader.hasNext()) {
            String trailerError = streamReader.getTrailerError();
            streamReader.close();
            response.close();
            if (trailerError != null) {
                throw new SandboxException("Failed to start process: " + trailerError);
            }
            throw new SandboxException("Failed to start process: no response from server");
        }

        StartResponse firstEvent = streamReader.next();
        if (!firstEvent.hasEvent() || !firstEvent.getEvent().hasStart()) {
            streamReader.close();
            response.close();
            throw new SandboxException("Failed to start process: invalid response from server");
        }

        long pid = firstEvent.getEvent().getStart().getPid();
        if (pid <= 0) {
            streamReader.close();
            response.close();
            throw new SandboxException("Failed to start process: invalid PID received");
        }

        return new CommandHandle(pid, streamReader, response, () -> kill((int) pid),
            options.getOnStdout(), options.getOnStderr());
    }

    public void sendInput(int pid, String data) {
        validatePid(pid);
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }

        SendInputRequest params = SendInputRequest.newBuilder()
            .setProcess(ProcessSelector.newBuilder().setPid(pid).build())
            .setInput(ProcessInput.newBuilder()
                .setStdin(com.google.protobuf.ByteString.copyFromUtf8(data))
                .build())
            .build();

        try {
            Request request = config.buildHttpRequest(EnvdMethods.PROCESS_SERVICE, EnvdMethods.PROCESS_SEND_INPUT, params, sandboxID);

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("SendInput HTTP request failed: " + response.code() + " " + response.message());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send input", e);
        }
    }

    public boolean kill(int pid) {
        return sendSignal(pid, Signal.SIGNAL_SIGKILL);
    }

    public boolean sendSignal(int pid, Signal signal) {
        validatePid(pid);

        SendSignalRequest params = SendSignalRequest.newBuilder()
            .setProcess(ProcessSelector.newBuilder().setPid((int)pid).build())
            .setSignal(signal != null ? signal : Signal.SIGNAL_UNSPECIFIED)
            .build();

        try {
            Request request = config.buildHttpRequest(EnvdMethods.PROCESS_SERVICE, EnvdMethods.PROCESS_SEND_SIGNAL, params, sandboxID);

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                    return false;
                }
                if (!response.isSuccessful()) {
                    throw new IOException("SendSignal HTTP request failed: " + response.code() + " " + response.message());
                }
                return response.isSuccessful();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send signal", e);
        }
    }

    public void closeStdin(int pid) {
        validatePid(pid);
        CloseStdinRequest params = CloseStdinRequest.newBuilder()
            .setProcess(ProcessSelector.newBuilder().setPid((int)pid).build())
            .build();

        try {
            Request request = config.buildHttpRequest(EnvdMethods.PROCESS_SERVICE, EnvdMethods.PROCESS_CLOSE_STDIN, params, sandboxID);

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("CloseStdin HTTP request failed: " + response.code() + " " + response.message());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to closeStdin", e);
        }
    }

    public CommandHandle connect(int pid) {
        validatePid(pid);

        ConnectRequest connectReq = ConnectRequest.newBuilder()
            .setProcess(ProcessSelector.newBuilder().setPid(pid).build())
            .build();

        // Initiate request via Connect Protocol Server-Streaming
        Request httpRequest = config.buildStreamingRequest(EnvdMethods.PROCESS_SERVICE, EnvdMethods.PROCESS_CONNECT, connectReq, sandboxID);
        Response response;
        try {
            // noinspection WithSSRFCheckingInspection
            response = streamingClient.newCall(httpRequest).execute();
        } catch (IOException e) {
            throw new SandboxException("Failed to connect to process", e);
        }
        if (!response.isSuccessful()) {
            String msg = "Connect HTTP request failed: " + response.code() + " " + response.message();
            response.close();
            throw new SandboxException(msg);
        }

        // Parse Connect Protocol frames from response stream
        // Connect method returns ConnectResponse, needs to be converted to StartResponse format
        InputStream responseStream = response.body().byteStream();
        ConnectStreamReader<ConnectResponse> connectReader =
            new ConnectStreamReader<>(responseStream, ConnectResponse.parser());

        if (!connectReader.hasNext()) {
            connectReader.close();
            response.close();
            throw new SandboxException("Failed to connect to process: no response from server");
        }

        ConnectResponse firstResponse = connectReader.next();
        if (!firstResponse.hasEvent()) {
            connectReader.close();
            response.close();
            throw new SandboxException("Failed to connect to process: invalid response from server");
        }

        ProcessEvent event = firstResponse.getEvent();
        if (!event.hasStart()) {
            connectReader.close();
            response.close();
            throw new SandboxException("Failed to connect to process: expected start event");
        }

        long connectedPid = event.getStart().getPid();
        if (connectedPid != pid) {
            connectReader.close();
            response.close();
            throw new SandboxException(
                "Failed to connect to process: PID mismatch, expected " + pid + " but got " + connectedPid);
        }

        // Adapt ConnectResponse stream to StartResponse stream
        MessageStream<StartResponse> adaptedReader =
            new ConnectResponseAdapter(connectReader, response);

        return new CommandHandle(pid, adaptedReader, response, () -> kill(pid), null, null);
    }

    private void validatePid(int pid) {
        if (pid <= 0) {
            throw new IllegalArgumentException("PID must be positive");
        }
    }

    /**
     * Adapter that adapts ConnectResponse stream to StartResponse stream.
     */
    private static class ConnectResponseAdapter implements MessageStream<StartResponse> {
        private final ConnectStreamReader<ConnectResponse> delegate;
        private final Response response;

        ConnectResponseAdapter(ConnectStreamReader<ConnectResponse> delegate, Response response) {
            this.delegate = delegate;
            this.response = response;
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public StartResponse next() {
            ConnectResponse connResponse = delegate.next();
            if (!connResponse.hasEvent()) {
                return StartResponse.getDefaultInstance();
            }
            return StartResponse.newBuilder()
                .setEvent(connResponse.getEvent())
                .build();
        }

        @Override
        public void close() {
            delegate.close();
            if (response != null) {
                try {
                    response.close();
                } catch (Exception ignored) {
                    // Ignore close exceptions to satisfy Closeable contract
                }
            }
        }
    }

    public static class RunOptions {
        /** Default shell, prefers /bin/bash, can be overridden to /bin/sh, etc. */
        private String shell;
        private String cwd;
        private Map<String, String> envs;
        private boolean stdin = false;
        private Long timeoutMs;
        private Consumer<String> onStdout;
        private Consumer<String> onStderr;

        /**
         * Sets the shell used to execute commands (default: /bin/bash).
         * If /bin/bash is not available in the sandbox, can be set to /bin/sh.
         */
        public RunOptions shell(String shell) {
            this.shell = shell;
            return this;
        }

        public RunOptions cwd(String cwd) {
            this.cwd = cwd;
            return this;
        }

        public RunOptions envs(Map<String, String> envs) {
            if (envs != null) {
                this.envs = new HashMap<String, String>(envs);
            }
            return this;
        }

        public RunOptions stdin(boolean stdin) {
            this.stdin = stdin;
            return this;
        }

        public RunOptions timeoutMs(long timeoutMs) {
            if (timeoutMs <= 0) {
                throw new IllegalArgumentException("Timeout must be positive");
            }
            this.timeoutMs = timeoutMs;
            return this;
        }

        public RunOptions onStdout(Consumer<String> onStdout) {
            this.onStdout = onStdout;
            return this;
        }

        public RunOptions onStderr(Consumer<String> onStderr) {
            this.onStderr = onStderr;
            return this;
        }

        public String getCwd() {
            return cwd;
        }

        public Map<String, String> getEnvs() {
            if (envs == null) {
                return null;
            }
            return Collections.unmodifiableMap(new HashMap<String, String>(envs));
        }

        public boolean isStdin() {
            return stdin;
        }

        public Long getTimeoutMs() {
            return timeoutMs;
        }

        public Consumer<String> getOnStdout() {
            return onStdout;
        }

        public Consumer<String> getOnStderr() {
            return onStderr;
        }

        public String getShell() {
            return shell;
        }
    }

    public static class ProcessInfo {
        private final long pid;
        private final String tag;
        private final String cmd;
        private final List<String> args;
        private final Map<String, String> envs;
        private final String cwd;

        public ProcessInfo(long pid, String tag, String cmd, List<String> args,
            Map<String, String> envs, String cwd) {
            if (pid <= 0) {
                throw new IllegalArgumentException("PID must be positive");
            }
            this.pid = pid;
            this.tag = tag;
            this.cmd = Objects.requireNonNull(cmd, "Command cannot be null");
            if (args != null) {
                this.args = Collections.unmodifiableList(new ArrayList<String>(args));
            } else {
                this.args = Collections.emptyList();
            }
            if (envs != null) {
                this.envs = Collections.unmodifiableMap(new HashMap<String, String>(envs));
            } else {
                this.envs = Collections.emptyMap();
            }
            this.cwd = cwd;
        }

        public long getPid() {
            return pid;
        }

        public String getTag() {
            return tag;
        }

        public String getCmd() {
            return cmd;
        }

        public List<String> getArgs() {
            return args;
        }

        public Map<String, String> getEnvs() {
            return envs;
        }

        public String getCwd() {
            return cwd;
        }

        @Override
        public String toString() {
            return String.format("ProcessInfo{pid=%d, cmd='%s', cwd='%s'}", pid, cmd, cwd);
        }
    }
}
