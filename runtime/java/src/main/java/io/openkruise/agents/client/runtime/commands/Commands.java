package io.openkruise.agents.client.runtime.commands;

import com.google.protobuf.util.JsonFormat;
import io.openkruise.agents.client.runtime.EnvdMethods;
import io.openkruise.agents.client.runtime.RuntimeConfig;
import io.openkruise.agents.client.runtime.envd.process.*;
import io.openkruise.agents.client.runtime.exceptions.SandboxException;
import io.openkruise.agents.client.runtime.utils.ConnectStreamReader;
import io.openkruise.agents.client.runtime.utils.MessageStream;
import okhttp3.Call;
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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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

    /**
     * Shared daemon scheduler for per-call request watchdogs. Watchdog tasks
     * only cancel an OkHttp Call (non-blocking), so a single thread is enough;
     * the daemon flag lets the JVM exit without shutting it down.
     * <p>
     * Built as a raw ScheduledThreadPoolExecutor (not via
     * Executors.newSingleThreadScheduledExecutor) so remove-on-cancel can be
     * enabled: every watchdog is cancel(false)d once the start event arrives
     * (successful start, or disarmed on failure), and without the policy each
     * canceled task stays in the delay queue until its original delay
     * elapses — holding the captured OkHttp Call and, through it, the whole
     * request/response chain from GC. The client-level default delay is
     * bounded (60s), but the per-call requestTimeoutMs options have no upper
     * limit, so large values under high concurrency would otherwise
     * accumulate linearly.
     * <p>
     * Applicability boundary: the scheduler is never shut down and its
     * thread pins this class's classloader for the JVM's lifetime. That is
     * fine for CLI and ordinary applications; in application servers or
     * hot-deploy environments a pinned classloader can block redeployment,
     * where hosting the SDK (and thus this scheduler) in a server-level
     * classloader is the expected workaround.
     */
    private static final ScheduledThreadPoolExecutor REQUEST_WATCHDOG_SCHEDULER =
        createWatchdogScheduler();

    private static ScheduledThreadPoolExecutor createWatchdogScheduler() {
        ScheduledThreadPoolExecutor scheduler =
            new ScheduledThreadPoolExecutor(1, runnable -> {
                Thread thread = new Thread(runnable, "commands-request-watchdog");
                thread.setDaemon(true);
                return thread;
            });
        // Canceled watchdog tasks leave the delay queue immediately instead
        // of lingering (and pinning the captured Call) until their delay
        // elapses — see the class javadoc above.
        scheduler.setRemoveOnCancelPolicy(true);
        return scheduler;
    }
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

        // Initiate request via Connect Protocol Server-Streaming.
        // A per-request user overrides the Authorization header so envd runs
        // the process as that user (Basic auth, empty password).
        Map<String, String> extraHeaders = null;
        if (options.getUser() != null && !options.getUser().isEmpty()) {
            if (options.getUser().contains(":")) {
                throw new IllegalArgumentException(
                    "Invalid user '" + options.getUser() + "': must not contain ':' "
                    + "(Basic auth would split it into a user:password pair)");
            }
            extraHeaders = new HashMap<>();
            extraHeaders.put("Authorization", RuntimeConfig.authHeaderForUser(options.getUser()));
        }
        Request httpRequest = config.buildStreamingRequest(EnvdMethods.PROCESS_SERVICE, EnvdMethods.PROCESS_START, startReq, sandboxID, extraHeaders);
        Call call = streamingClient.newCall(httpRequest);
        // Overall command timeout (RunOptions.timeoutMs): bounds the whole
        // call — connect handshake, start event and output streaming until
        // the end event (official E2B timeout semantics). Null or unset means
        // no limit: the official SDK defaults to 60s, while unset keeps the
        // stream unbounded here — set it explicitly if you want the official
        // default. On timeout OkHttp cancels the call and waitForCompletion
        // fails, while the process keeps running in the sandbox.
        if (options.getTimeoutMs() != null && options.getTimeoutMs() > 0) {
            call.timeout().timeout(options.getTimeoutMs(), TimeUnit.MILLISECONDS);
        }
        long requestTimeoutMs = options.getRequestTimeoutMs() != null
            ? options.getRequestTimeoutMs()
            : config.getRequestTimeoutMs();
        // Names the source that actually armed the watchdog in timeout
        // messages: the per-call option, or the client-level fallback.
        String requestTimeoutSource = options.getRequestTimeoutMs() != null
            ? "RunOptions.requestTimeoutMs"
            : "client-level request timeout";
        // Arm the watchdog before the blocking call: execute() waits for the
        // response headers, so the watchdog must already be running to bound
        // that wait. Official E2B semantics: request_timeout bounds opening
        // the stream (the wait for the start event); once the start event
        // arrives the watchdog is disarmed and the rest of the stream is not
        // bounded by it. Zero (or negative) disables the wait — the official
        // explicit "request_timeout=0" semantics: the stream then blocks
        // until the start event arrives with no SDK-side bound. On timeout
        // the stream fails while the process keeps running in the sandbox.
        AtomicBoolean watchdogFired = new AtomicBoolean(false);
        // Inherent narrow race (mirror of the Go-side note in commands.go):
        // when the watchdog fires in the same instant the start event
        // arrives, the call is already canceled while runBackground still
        // returns a handle — waitForCompletion() then fails with a stream
        // error instead of a timeout message, and when timeoutMs is also
        // set, the structural isCallTimeout detection attributes it to
        // RunOptions.timeoutMs. The window is microseconds, nothing leaks;
        // accepted.
        ScheduledFuture<?> requestWatchdog = requestTimeoutMs > 0
            ? REQUEST_WATCHDOG_SCHEDULER.schedule(() -> {
                watchdogFired.set(true);
                call.cancel();
            }, requestTimeoutMs, TimeUnit.MILLISECONDS)
            : null;

        Response response;
        try {
            // noinspection WithSSRFCheckingInspection
            response = call.execute();
        } catch (IOException e) {
            if (requestWatchdog != null) {
                requestWatchdog.cancel(false);
            }
            if (watchdogFired.get()) {
                throw new SandboxException("Timed out after " + requestTimeoutMs
                    + "ms waiting for the start event (" + requestTimeoutSource + ";"
                    + " the process may still be running in the sandbox)", e);
            }
            // Mirror of Go's overallTimedOut attribution: when the overall
            // RunOptions.timeoutMs deadline expires before the start event,
            // OkHttp cancels the call and execute() throws. Nothing else can
            // cancel the call at this point (the handle does not exist yet,
            // and the request watchdog is checked above), so the canceled
            // state uniquely identifies the overall timeout.
            if (options.getTimeoutMs() != null && options.getTimeoutMs() > 0 && call.isCanceled()) {
                throw new SandboxException("Timed out after " + options.getTimeoutMs()
                    + "ms (RunOptions.timeoutMs; the process may still be running in the sandbox)", e);
            }
            throw new SandboxException("Failed to start process", e);
        }
        if (!response.isSuccessful()) {
            if (requestWatchdog != null) {
                requestWatchdog.cancel(false);
            }
            String msg = "Start HTTP request failed: " + response.code() + " " + response.message();
            response.close();
            throw new SandboxException(msg);
        }

        // Parse Connect Protocol frames from response stream
        InputStream responseStream = response.body().byteStream();
        ConnectStreamReader<StartResponse> streamReader =
            new ConnectStreamReader<>(responseStream, StartResponse.parser());

        long pid;
        boolean startEventConfirmed = false;
        try {
            if (!streamReader.hasNext()) {
                String trailerError = streamReader.getTrailerError();
                if (watchdogFired.get()) {
                    // hasNext() stashed the IOException raised when the
                    // watchdog canceled the call, so keep it as the cause.
                    throw new SandboxException("Timed out after " + requestTimeoutMs
                        + "ms waiting for the start event (" + requestTimeoutSource + ";"
                        + " the process may still be running in the sandbox)",
                        streamReader.getLastError());
                }
                if (trailerError != null) {
                    throw new SandboxException("Failed to start process: " + trailerError);
                }
                throw new SandboxException("Failed to start process: no response from server");
            }

            StartResponse firstEvent = streamReader.next();
            if (!firstEvent.hasEvent() || !firstEvent.getEvent().hasStart()) {
                throw new SandboxException("Failed to start process: invalid response from server");
            }

            pid = firstEvent.getEvent().getStart().getPid();
            if (pid <= 0) {
                throw new SandboxException("Failed to start process: invalid PID received");
            }
            // Ownership handoff: once startEventConfirmed is set the finally
            // block no longer closes the stream — the handle constructed
            // below takes it over. The CommandHandle constructor is plain
            // field assignment (pid is validated positive above), so nothing
            // can throw between the handoff and the return; keep that
            // invariant if the constructor ever grows.
            startEventConfirmed = true;
        } finally {
            // Close exactly once on every failure path — including an
            // unexpected exception escaping the block above; on success the
            // handle takes ownership of the stream and response. Also disarm
            // the request watchdog so it never fires mid-stream (null when
            // the wait was disabled, requestTimeoutMs <= 0).
            if (!startEventConfirmed) {
                streamReader.close();
                response.close();
            }
            if (requestWatchdog != null) {
                requestWatchdog.cancel(false);
            }
        }

        CommandHandle handle = new CommandHandle(pid, streamReader, response,
            () -> killOrThrow((int) pid),
            call::cancel, options.getOnStdout(), options.getOnStderr());
        // Pass the armed call timeout and the call's canceled state so
        // waitForCompletion can attribute a stream cut by it to
        // RunOptions.timeoutMs (structural detection via isCanceled,
        // not error-text matching). Ownership transfers to the caller via
        // the return below; the handle stays open as the caller's
        // AutoCloseable.
        handle.withTimeoutMs(options.getTimeoutMs(), "RunOptions.timeoutMs");
        handle.withCallCanceled(call::isCanceled);
        return handle;
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

    /** Kill action for CommandHandle: sends SIGKILL, surfacing envd's
     * "process not found" (HTTP 404, kill returning false) as an
     * IllegalStateException so CommandHandle.kill rolls its completion
     * claim back and lets the kill be retried — the same real
     * outcome the Go handleKill reports with its boolean. Called lazily
     * when the handle's kill() runs, never at construction time. */
    private void killOrThrow(int pid) {
        if (!kill(pid)) {
            throw new IllegalStateException(
                "Process " + pid + " not found; no signal was sent");
        }
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
        return connect(pid, null);
    }

    /**
     * Connects to a running process by PID, with per-call options. The
     * official E2B connect offers the same two per-call limits as start;
     * previously only the client-level request timeout applied here.
     *
     * @param pid     PID of the process to connect to
     * @param options per-call limits; null or unset falls back to the
     *                client-level request timeout, and leaves the whole
     *                stream unbounded
     */
    public CommandHandle connect(int pid, ConnectOptions options) {
        validatePid(pid);
        if (options == null) {
            options = new ConnectOptions();
        }

        ConnectRequest connectReq = ConnectRequest.newBuilder()
            .setProcess(ProcessSelector.newBuilder().setPid(pid).build())
            .build();

        // Initiate request via Connect Protocol Server-Streaming
        Request httpRequest = config.buildStreamingRequest(EnvdMethods.PROCESS_SERVICE, EnvdMethods.PROCESS_CONNECT, connectReq, sandboxID);
        // Keep the Call reference so the returned handle can cancel the
        // stream without killing the process (CommandHandle.disconnect()).
        Call call = streamingClient.newCall(httpRequest);
        // Overall connect-stream timeout (ConnectOptions.timeoutMs): bounds
        // the whole call — connect handshake, start event and output
        // streaming until the end event (official E2B connect timeout
        // semantics; previously this path had no overall limit at all).
        // Null or unset means no limit; on timeout OkHttp cancels the call
        // and waitForCompletion fails, while the process keeps running.
        if (options.getTimeoutMs() != null && options.getTimeoutMs() > 0) {
            call.timeout().timeout(options.getTimeoutMs(), TimeUnit.MILLISECONDS);
        }
        // Arm the request-timeout watchdog like the Start path (and the Go
        // ConnectToProcess): streamingClient has no read timeout, so
        // without this a server that accepts the connection but never
        // answers would block call.execute()/the first frame read forever
        // — with no handle returned yet for the caller to disconnect.
        // Official semantics: the request timeout bounds opening the stream
        // (the wait for the start event); ConnectOptions.requestTimeoutMs
        // overrides the client-level value per call. Zero (or negative)
        // disables the wait, matching requestTimeoutMs(0).
        long requestTimeoutMs = options.getRequestTimeoutMs() != null
            ? options.getRequestTimeoutMs()
            : config.getRequestTimeoutMs();
        // Names the source that actually armed the watchdog in timeout
        // messages: the per-call option, or the client-level fallback.
        String requestTimeoutSource = options.getRequestTimeoutMs() != null
            ? "ConnectOptions.requestTimeoutMs"
            : "client-level request timeout";
        AtomicBoolean watchdogFired = new AtomicBoolean(false);
        // Timeout message shared by both watchdog checks below (Go side
        // keeps the same text in its watchdogTimedOut closure).
        java.util.function.Supplier<String> watchdogTimeoutMessage = () -> "Timed out after " + requestTimeoutMs
            + "ms waiting for the connect event (" + requestTimeoutSource + ";"
            + " the process may still be running in the sandbox)";
        ScheduledFuture<?> requestWatchdog = requestTimeoutMs > 0
            ? REQUEST_WATCHDOG_SCHEDULER.schedule(() -> {
                watchdogFired.set(true);
                call.cancel();
            }, requestTimeoutMs, TimeUnit.MILLISECONDS)
            : null;

        Response response;
        try {
            // noinspection WithSSRFCheckingInspection
            response = call.execute();
        } catch (IOException e) {
            if (requestWatchdog != null) {
                requestWatchdog.cancel(false);
            }
            if (watchdogFired.get()) {
                throw new SandboxException(watchdogTimeoutMessage.get(), e);
            }
            // Mirror of the start-path attribution: when the overall
            // ConnectOptions.timeoutMs deadline expires before the start
            // event, OkHttp cancels the call and execute() throws. Nothing
            // else can cancel the call at this point (the handle does not
            // exist yet, and the request watchdog is checked above), so the
            // canceled state uniquely identifies the overall timeout.
            if (options.getTimeoutMs() != null && options.getTimeoutMs() > 0 && call.isCanceled()) {
                throw new SandboxException("Timed out after " + options.getTimeoutMs()
                    + "ms (ConnectOptions.timeoutMs; the process may still be running in the sandbox)", e);
            }
            throw new SandboxException("Failed to connect to process", e);
        }
        if (!response.isSuccessful()) {
            if (requestWatchdog != null) {
                requestWatchdog.cancel(false);
            }
            String msg = "Connect HTTP request failed: " + response.code() + " " + response.message();
            response.close();
            throw new SandboxException(msg);
        }

        // Parse Connect Protocol frames from response stream
        // Connect method returns ConnectResponse, needs to be converted to StartResponse format
        InputStream responseStream = response.body().byteStream();
        ConnectStreamReader<ConnectResponse> connectReader =
            new ConnectStreamReader<>(responseStream, ConnectResponse.parser());

        boolean startEventConfirmed = false;
        try {
            if (!connectReader.hasNext()) {
                if (watchdogFired.get()) {
                    // Same as the start path: keep the IOException stashed by
                    // hasNext() when the watchdog canceled the call.
                    throw new SandboxException(watchdogTimeoutMessage.get(),
                        connectReader.getLastError());
                }
                // Same as the start path: surface a server error delivered
                // in the end-of-stream trailer instead of the generic
                // "no response from server" (the Go side reads no trailer
                // on either path — Java-only fidelity, no divergence).
                String trailerError = connectReader.getTrailerError();
                if (trailerError != null) {
                    throw new SandboxException("Failed to connect to process: " + trailerError);
                }
                throw new SandboxException("Failed to connect to process: no response from server");
            }

            ConnectResponse firstResponse = connectReader.next();
            if (!firstResponse.hasEvent()) {
                throw new SandboxException("Failed to connect to process: invalid response from server");
            }

            ProcessEvent event = firstResponse.getEvent();
            if (!event.hasStart()) {
                throw new SandboxException("Failed to connect to process: expected start event");
            }

            long connectedPid = event.getStart().getPid();
            if (connectedPid != pid) {
                throw new SandboxException(
                    "Failed to connect to process: PID mismatch, expected " + pid + " but got " + connectedPid);
            }

            // Ownership handoff: once startEventConfirmed is set the finally
            // block no longer closes the stream — the handle constructed
            // below takes it over. Both the adapter and the CommandHandle
            // constructor are plain field assignment (pid is validated
            // above), so nothing can throw between the handoff and the
            // return; keep that invariant if either ever grows.
            startEventConfirmed = true;

            // Adapt ConnectResponse stream to StartResponse stream
            MessageStream<StartResponse> adaptedReader =
                new ConnectResponseAdapter(connectReader, response);

            CommandHandle handle = new CommandHandle(pid, adaptedReader, response,
                () -> killOrThrow(pid),
                call::cancel, null, null);
            // Pass the armed call timeout and the call's canceled state
            // so waitForCompletion can attribute a stream cut by it to
            // ConnectOptions.timeoutMs (structural detection via
            // isCanceled, not error-text matching). Ownership transfers
            // to the caller via the return below; the handle stays open
            // as the caller's AutoCloseable.
            handle.withTimeoutMs(options.getTimeoutMs(), "ConnectOptions.timeoutMs");
            handle.withCallCanceled(call::isCanceled);
            return handle;
        } finally {
            // Close exactly once on every failure path — including an
            // unexpected exception escaping the block above; on success the
            // handle takes ownership of the stream and response. Also disarm
            // the request watchdog so it never fires mid-stream (null when
            // the wait was disabled, requestTimeoutMs <= 0).
            if (!startEventConfirmed) {
                connectReader.close();
                response.close();
            }
            if (requestWatchdog != null) {
                requestWatchdog.cancel(false);
            }
        }
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
        public java.io.IOException getLastError() {
            return delegate.getLastError();
        }

        @Override
        public String getTrailerError() {
            return delegate.getTrailerError();
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

    /**
     * Per-call options for {@link #connect(int, ConnectOptions)}. Mirrors the
     * official E2B connect capability, which offers the same two per-call
     * limits as start.
     */
    public static class ConnectOptions {
        private Long timeoutMs;
        private Long requestTimeoutMs;

        /**
         * Sets the overall time limit in milliseconds for the whole
         * connect stream — connect handshake, start event and output
         * streaming until the end event (official E2B connect timeout
         * semantics). Zero or unset means no limit — the official
         * timeout-to-ms treats 0 as unlimited, and unlike the start-path
         * default this SDK keeps unset streams unbounded. On timeout the
         * stream fails with an exception while the process keeps running
         * in the sandbox (kill it explicitly via CommandHandle.kill() if
         * needed).
         */
        public ConnectOptions timeoutMs(long timeoutMs) {
            if (timeoutMs < 0) {
                throw new IllegalArgumentException("Timeout must not be negative (zero means no limit)");
            }
            this.timeoutMs = timeoutMs;
            return this;
        }

        /**
         * Sets the time limit in milliseconds for waiting for the connect
         * start event. Once the start event arrives the limit is disarmed
         * and does not restrict how long the process may keep streaming
         * output afterwards. Zero disables the wait (the official
         * explicit-disable semantics); when unset, falls back to the
         * client-level request timeout (60s by default).
         */
        public ConnectOptions requestTimeoutMs(long requestTimeoutMs) {
            if (requestTimeoutMs < 0) {
                throw new IllegalArgumentException("RequestTimeout must not be negative (zero disables the wait)");
            }
            this.requestTimeoutMs = requestTimeoutMs;
            return this;
        }

        public Long getTimeoutMs() {
            return timeoutMs;
        }

        public Long getRequestTimeoutMs() {
            return requestTimeoutMs;
        }
    }

    public static class RunOptions {
        /** Default shell, prefers /bin/bash, can be overridden to /bin/sh, etc. */
        private String shell;
        private String cwd;
        private String user;
        private Map<String, String> envs;
        private boolean stdin = false;
        private Long timeoutMs;
        private Long requestTimeoutMs;
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

        /**
         * Sets the user to run the command as (sent via the Authorization
         * header; the user must exist inside the sandbox image; must not
         * contain ':', which Basic auth would split into a user:password
         * pair). The override applies to the process start request only
         * (run/runBackground); other process RPCs (list, kill, sendInput,
         * connect) keep the default header. When unset, the configured
         * Authorization header is used (root by default).
         */
        public RunOptions user(String user) {
            this.user = user;
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

        /**
         * Sets the overall time limit in milliseconds for the whole command
         * stream — connect handshake, start event and output streaming until
         * the end event (official E2B timeout semantics). Zero or unset means
         * no limit, like the official timeout-to-ms: the official SDK
         * defaults to 60 seconds, while unset keeps the stream unbounded
         * here — set 60000 explicitly if you want the official default. On
         * timeout the stream fails with an exception while the process keeps
         * running in the sandbox (kill it explicitly via
         * CommandHandle.kill() if needed).
         */
        public RunOptions timeoutMs(long timeoutMs) {
            if (timeoutMs < 0) {
                throw new IllegalArgumentException("Timeout must not be negative (zero means no limit)");
            }
            this.timeoutMs = timeoutMs;
            return this;
        }

        /**
         * Sets the time limit in milliseconds for waiting the command to
         * start — the wait until envd confirms with a start event. Once the
         * start event arrives the limit is disarmed and does not restrict
         * how long the command may keep running or streaming output
         * afterwards. Zero disables the wait for the start event (the
         * official explicit-disable semantics); when unset, falls back to
         * the client-level request timeout (60s by default).
         */
        public RunOptions requestTimeoutMs(long requestTimeoutMs) {
            if (requestTimeoutMs < 0) {
                throw new IllegalArgumentException("RequestTimeout must not be negative (zero disables the wait)");
            }
            this.requestTimeoutMs = requestTimeoutMs;
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

        public String getUser() {
            return user;
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

        public Long getRequestTimeoutMs() {
            return requestTimeoutMs;
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
