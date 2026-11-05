package io.openkruise.agents.client.runtime.commands;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.openkruise.agents.client.runtime.RuntimeConfig;
import io.openkruise.agents.client.runtime.envd.process.ConnectResponse;
import io.openkruise.agents.client.runtime.envd.process.ProcessEvent;
import io.openkruise.agents.client.runtime.envd.process.StartResponse;
import io.openkruise.agents.client.runtime.exceptions.SandboxException;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Behavior tests for the Connect Protocol command paths, using a local
 * in-process HTTP server that speaks the same wire format as envd
 * (envelope-framed server-streaming responses, mirror of the Go-side
 * httptest suites in runtime/commands_test.go). These lock in the semantics
 * past code reviews kept catching by hand:
 * <ul>
 *   <li>truncated streams fail loudly instead of returning partial results</li>
 *   <li>a failed wait is sticky — a second call replays the failure</li>
 *   <li>kill() stays usable after a failed wait (the process may still run)</li>
 *   <li>a non-zero exit code throws (official E2B contract) and the
 *       exception carries the full result</li>
 *   <li>the request-timeout watchdog bounds opening Start/Connect streams</li>
 *   <li>disconnect() drops the stream without killing the process</li>
 * </ul>
 */
public class CommandsTest {

    // ------------------------------------------------------------------
    // Wire-format helpers: Connect Protocol envelope frames are
    // 1 byte flags + 4 bytes big-endian length + payload. A trailer frame
    // (flags & 0x02) closes the stream; its payload is the JSON
    // connectEndStreamMessage ("{}" on success).
    // ------------------------------------------------------------------

    private static byte[] envelopeFrame(int flags, byte[] payload) {
        byte[] out = new byte[5 + payload.length];
        out[0] = (byte) flags;
        out[1] = (byte) ((payload.length >> 24) & 0xFF);
        out[2] = (byte) ((payload.length >> 16) & 0xFF);
        out[3] = (byte) ((payload.length >> 8) & 0xFF);
        out[4] = (byte) (payload.length & 0xFF);
        System.arraycopy(payload, 0, out, 5, payload.length);
        return out;
    }

    private static byte[] dataFrame(Message message) {
        return envelopeFrame(0x00, message.toByteArray());
    }

    private static byte[] endOfStreamFrame() {
        return envelopeFrame(0x02, "{}".getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] errorTrailerFrame(String json) {
        return envelopeFrame(0x02, json.getBytes(StandardCharsets.UTF_8));
    }

    // ------------------------------------------------------------------
    // Event builders matching the envd process proto shapes.
    // ------------------------------------------------------------------

    private static StartResponse startEvent(int pid) {
        return StartResponse.newBuilder()
            .setEvent(ProcessEvent.newBuilder()
                .setStart(ProcessEvent.StartEvent.newBuilder().setPid(pid))
                .build())
            .build();
    }

    private static StartResponse stdoutData(String text) {
        return StartResponse.newBuilder()
            .setEvent(ProcessEvent.newBuilder()
                .setData(ProcessEvent.DataEvent.newBuilder()
                    .setStdout(ByteString.copyFromUtf8(text)))
                .build())
            .build();
    }

    private static StartResponse endEvent(int exitCode) {
        return endEvent(exitCode, "");
    }

    private static StartResponse endEvent(int exitCode, String error) {
        return StartResponse.newBuilder()
            .setEvent(ProcessEvent.newBuilder()
                .setEnd(ProcessEvent.EndEvent.newBuilder().setExitCode(exitCode).setError(error))
                .build())
            .build();
    }

    private static ConnectResponse connectStartEvent(int pid) {
        return ConnectResponse.newBuilder()
            .setEvent(ProcessEvent.newBuilder()
                .setStart(ProcessEvent.StartEvent.newBuilder().setPid(pid))
                .build())
            .build();
    }

    // ------------------------------------------------------------------
    // In-process mock envd. Streaming requests (/Start, /Connect) are
    // answered by the per-test script; unary calls (/SendSignal etc.)
    // answer 200 so kill() works. Go-side lessons applied here: the
    // blocking handlers are released by close() BEFORE server.stop() so
    // stop() never deadlocks waiting on a stuck handler, and every frame
    // write is flushed before blocking so the client sees it.
    // ------------------------------------------------------------------

    private interface StreamingScript {
        void handle(MockEnvd envd, HttpExchange exchange, String path) throws IOException;
    }

    private static final class MockEnvd implements AutoCloseable {
        final HttpServer server;
        final List<String> requestPaths = Collections.synchronizedList(new ArrayList<String>());
        /** When set, /SendSignal answers 500 so the kill RPC fails. */
        volatile boolean failSignal;
        /** When set, /SendSignal answers 404 (no process to signal), so
         * the kill action must surface the outcome instead of silently
         * claiming completion. */
        volatile boolean signalNotFound;
        /** Counts down when a /SendSignal (kill) request arrives, so a
         * streaming script can stage "what the server sends after the
         * kill" frames. */
        final CountDownLatch signalReceived = new CountDownLatch(1);
        private final CountDownLatch releaseBlockedHandlers = new CountDownLatch(1);

        private MockEnvd(StreamingScript script) throws IOException {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> {
                String path = exchange.getRequestURI().getPath();
                requestPaths.add(path);
                // Drain the request body (OkHttp streams it).
                exchange.getRequestBody().close();
                if (path.endsWith("/SendSignal")) {
                    signalReceived.countDown();
                    if (signalNotFound) {
                        exchange.sendResponseHeaders(404, -1);
                    } else if (failSignal) {
                        exchange.sendResponseHeaders(500, -1);
                    } else {
                        exchange.sendResponseHeaders(200, -1);
                    }
                    exchange.close();
                    return;
                }
                script.handle(this, exchange, path);
            });
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
        }

        static MockEnvd start(StreamingScript script) throws IOException {
            return new MockEnvd(script);
        }

        String baseUrl() {
            return "http://127.0.0.1:" + server.getAddress().getPort();
        }

        /** Answers the streaming call with the given frames and finishes. */
        void respond(HttpExchange exchange, byte[]... frames) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/connect+proto");
            exchange.sendResponseHeaders(200, 0);
            OutputStream out = exchange.getResponseBody();
            for (byte[] frame : frames) {
                out.write(frame);
            }
            out.flush();
            out.close();
        }

        /** Sends the frames, flushes them to the client, then blocks until
         * close() releases this handler (a command still running). */
        void respondThenBlock(HttpExchange exchange, byte[]... frames) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/connect+proto");
            exchange.sendResponseHeaders(200, 0);
            OutputStream out = exchange.getResponseBody();
            for (byte[] frame : frames) {
                out.write(frame);
            }
            out.flush(); // push the frames before blocking (buffered otherwise)
            try {
                releaseBlockedHandlers.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            out.close();
        }

        /** Never answers the request (a server that accepts the connection
         * but never responds) — released by close(). */
        void blockWithoutResponse(HttpExchange exchange) {
            try {
                releaseBlockedHandlers.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            try {
                exchange.sendResponseHeaders(500, -1);
            } catch (IOException ignored) {
                // The client likely canceled the call already; nothing to answer.
            }
            exchange.close();
        }

        /** Sends the beforeKill frames, flushes them, blocks until a kill
         * RPC arrives, then sends the afterKill frames and ends the stream
         * with no end event.
         * <p>
         * Deliberately free of timing margins. The test gates kill() on the
         * stdout callback of the last beforeKill frame, and
         * processResponse appends to stdout before running that callback,
         * so every frame the test asserts on is provably collected before
         * the completion claim exists. Which of the afterKill frames the
         * waiter still reads depends on whether the claim landed before or
         * after the read loop's post-frame check — a window no latch can
         * order — so the test asserts only what holds under every
         * interleaving. Ending the stream with no end event keeps a real
         * exit code from overwriting the synthetic one, which is what the
         * previous 400ms pause tried to hold back by force. */
        void respondAfterKillSignal(HttpExchange exchange, byte[][] beforeKillFrames,
                                    byte[]... afterKillFrames) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/connect+proto");
            exchange.sendResponseHeaders(200, 0);
            OutputStream out = exchange.getResponseBody();
            try {
                for (byte[] frame : beforeKillFrames) {
                    out.write(frame);
                }
                out.flush();
                try {
                    // Bounded like respondAfterLatch: a regression that never
                    // sends the kill RPC must fail the assertions, not hang
                    // server.stop(0) on a blocked handler.
                    signalReceived.await(10, java.util.concurrent.TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                for (byte[] frame : afterKillFrames) {
                    out.write(frame);
                }
                out.flush();
                out.close();
            } catch (IOException ignored) {
                // The client closed the stream when the kill claim broke the
                // read loop — exactly what this script stages.
            }
        }

        /** Sends the beforeFrames, flushes them, then blocks until a
         * test-owned latch counts down, then sends the late frames and
         * finishes. Unlike the server-side signalReceived latch (which
         * fires when the kill RPC merely arrives), the test releases this
         * one after an event it can observe client-side — e.g. kill()
         * throwing, which happens after the rollback — so the late frames
         * have a proper happens-before with it and the staged order is
         * deterministic. The await is bounded: a regression that never
         * releases the latch must fail the assertions, not hang
         * server.stop(0) on a blocked handler. */
        void respondAfterLatch(HttpExchange exchange, CountDownLatch latch,
                               byte[][] beforeFrames, byte[]... lateFrames) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/connect+proto");
            exchange.sendResponseHeaders(200, 0);
            OutputStream out = exchange.getResponseBody();
            for (byte[] frame : beforeFrames) {
                out.write(frame);
            }
            out.flush();
            try {
                latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            for (byte[] frame : lateFrames) {
                out.write(frame);
            }
            out.flush();
            out.close();
        }

        boolean receivedKillRequest() {
            synchronized (requestPaths) {
                for (String path : requestPaths) {
                    if (path.endsWith("/SendSignal")) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void close() {
            // Release blocked handlers first: server.stop() waits for them,
            // so releasing after stop() would deadlock (Go-side LIFO-cleanup
            // lesson). signalReceived is released for the same reason: a
            // respondAfterKillSignal handler waiting on it — a regression
            // that stops issuing the kill RPC would strand one — would
            // otherwise hang server.stop(0) forever, turning a failed
            // assertion into a hung build. (respondAfterLatch instead uses
            // a bounded await, so its test-owned latch needs no release.)
            releaseBlockedHandlers.countDown();
            signalReceived.countDown();
            server.stop(0);
        }
    }

    private static Commands commandsFor(MockEnvd envd, long clientRequestTimeoutMs) {
        RuntimeConfig config = new RuntimeConfig.Builder()
            .runtimeUrl(envd.baseUrl())
            .requestTimeoutMs(clientRequestTimeoutMs)
            .build();
        return new Commands("sbx-test", config,
            config.getOrCreateHttpClient(), config.getOrCreateStreamingHttpClient());
    }

    // ------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------

    @Test(timeout = 15000)
    public void runCollectsOutputAndExitCode() throws Exception {
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respond(exchange,
                dataFrame(startEvent(42)),
                dataFrame(stdoutData("hello")),
                dataFrame(endEvent(0)),
                endOfStreamFrame()))) {
            Commands commands = commandsFor(envd, 60_000L);
            CommandResult result = commands.run("echo hi");
            assertEquals("hello", result.getStdout());
            assertEquals(0, result.getExitCode());
        }
    }

    @Test(timeout = 15000)
    public void runThrowsOnNonZeroExitCode() throws Exception {
        // Official E2B contract (Python CommandExitException, JS
        // CommandExitError, this repo's Go CommandExitError): a non-zero
        // exit code throws, and the exception carries the full result so
        // stdout/stderr/exit code are not lost in the error path.
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respond(exchange,
                dataFrame(startEvent(42)),
                dataFrame(stdoutData("boom")),
                dataFrame(endEvent(3)),
                endOfStreamFrame()))) {
            Commands commands = commandsFor(envd, 60_000L);
            try {
                commands.run("false");
                fail("expected a CommandExitException for exit code 3");
            } catch (CommandExitException e) {
                assertEquals(3, e.getExitCode());
                assertEquals("boom", e.getStdout());
            }
        }
    }

    @Test(timeout = 15000)
    public void endEventErrorIsPreserved() throws Exception {
        // The end event may carry a server-side error message (envd reports
        // e.g. spawn failures there). The official SDKs preserve it (Python
        // CommandResult.error, JS CommandExitError.error, Go
        // CommandExitError.ErrorMessage); the exception message mirrors the
        // Go shape "command exited with code X: <error>".
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respond(exchange,
                dataFrame(startEvent(42)),
                dataFrame(endEvent(3, "failed to spawn")),
                endOfStreamFrame()))) {
            Commands commands = commandsFor(envd, 60_000L);
            try {
                commands.run("false");
                fail("expected a CommandExitException for exit code 3");
            } catch (CommandExitException e) {
                assertEquals("failed to spawn", e.getError());
                assertEquals(3, e.getExitCode());
                assertTrue("message must carry the end-event error, got: " + e.getMessage(),
                    e.getMessage().contains("failed to spawn"));
            }
        }
    }

    @Test(timeout = 15000)
    public void nonZeroExitFailureIsSticky() throws Exception {
        // The non-zero-exit exception is sticky like a stream failure: a
        // second waitForCompletion() replays it instead of returning the
        // result.
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respond(exchange,
                dataFrame(startEvent(42)),
                dataFrame(endEvent(3)),
                endOfStreamFrame()))) {
            Commands commands = commandsFor(envd, 60_000L);
            CommandHandle handle = commands.runBackground("x", new Commands.RunOptions());
            try {
                handle.waitForCompletion();
                fail("expected a CommandExitException");
            } catch (CommandExitException first) {
                try {
                    handle.waitForCompletion();
                    fail("expected the second call to replay the exit exception");
                } catch (CommandExitException second) {
                    assertEquals(first.getMessage(), second.getMessage());
                }
            }
        }
    }

    @Test(timeout = 15000)
    public void truncatedStreamThrowsAndFailureIsSticky() throws Exception {
        // A stream that ends without an end event must fail loudly — and the
        // failure must be sticky: a second waitForCompletion() replays the
        // exception instead of silently returning the partial result
        // (exitCode -1) it used to return after the finally block marked the
        // handle completed.
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respond(exchange,
                dataFrame(startEvent(42)),
                dataFrame(stdoutData("partial")),
                endOfStreamFrame()))) {
            Commands commands = commandsFor(envd, 60_000L);
            CommandHandle handle = commands.runBackground("x", new Commands.RunOptions());

            String first = assertWaitThrows(handle);
            assertTrue("expected 'no end event' failure, got: " + first,
                first.contains("without an end event"));
            // Second call replays the same failure — never a partial result.
            String second = assertWaitThrows(handle);
            assertEquals(first, second);
        }
    }

    @Test(timeout = 15000)
    public void killAfterFailedWaitDoesNotSwallowFailure() throws Exception {
        // Order-of-checks regression guard: kill() sets completed, so an
        // entry check of completed-before-failure would answer a later
        // waitForCompletion() with a synthetic exit-code -1 result and
        // swallow the real stream failure. failure must win over completed.
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respond(exchange,
                dataFrame(startEvent(42)),
                dataFrame(stdoutData("partial")),
                endOfStreamFrame()))) {
            Commands commands = commandsFor(envd, 60_000L);
            CommandHandle handle = commands.runBackground("x", new Commands.RunOptions());

            String failureMessage = assertWaitThrows(handle);
            assertTrue(handle.kill());
            String afterKill = assertWaitThrows(handle);
            assertEquals("kill() must not swallow the recorded stream failure",
                failureMessage, afterKill);
        }
    }

    @Test(timeout = 15000)
    public void killThenWaitReturnsResult() throws Exception {
        // A wait made only after kill() returns the synthetic result
        // (exit code -1) — there is no end event to wait for.
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respondThenBlock(exchange, dataFrame(startEvent(42))))) {
            Commands commands = commandsFor(envd, 60_000L);
            CommandHandle handle = commands.runBackground("sleep 60", new Commands.RunOptions());
            assertTrue(handle.kill());
            CommandResult result = handle.waitForCompletion();
            assertEquals(-1, result.getExitCode());
        }
    }

    @Test(timeout = 15000)
    public void killStillWorksAfterFailedWait() throws Exception {
        // The fail-loudly contract keeps kill() usable: the process may
        // still be running in the sandbox when the stream failed.
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respond(exchange,
                dataFrame(startEvent(42)),
                dataFrame(stdoutData("partial")),
                endOfStreamFrame()))) {
            Commands commands = commandsFor(envd, 60_000L);
            CommandHandle handle = commands.runBackground("x", new Commands.RunOptions());

            assertWaitThrows(handle);
            assertTrue("kill() must stay usable after a failed wait", handle.kill());
            assertTrue("kill() must have issued a SendSignal RPC",
                envd.receivedKillRequest());
        }
    }

    @Test(timeout = 15000)
    public void startRequestWatchdogTimesOut() throws Exception {
        // Server accepts the connection but never answers: the per-call
        // requestTimeoutMs watchdog must cut the wait and name the option.
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.blockWithoutResponse(exchange))) {
            Commands commands = commandsFor(envd, 60_000L);
            try {
                commands.runBackground("x", new Commands.RunOptions().requestTimeoutMs(150));
                fail("expected a request-timeout SandboxException");
            } catch (SandboxException e) {
                assertTrue("expected start-event timeout, got: " + e.getMessage(),
                    e.getMessage().contains("waiting for the start event"));
                assertTrue("expected the per-call option named, got: " + e.getMessage(),
                    e.getMessage().contains("RunOptions.requestTimeoutMs"));
            }
        }
    }

    @Test(timeout = 15000)
    public void overallTimeoutBeforeStartEventAttributedToTimeoutMs() throws Exception {
        // Server accepts the connection but never answers: with the start
        // watchdog disabled (requestTimeoutMs(0)) the overall timeoutMs
        // deadline must cut the wait and name the option, mirroring Go's
        // overallTimedOut attribution.
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.blockWithoutResponse(exchange))) {
            Commands commands = commandsFor(envd, 60_000L);
            try {
                commands.runBackground("x", new Commands.RunOptions()
                    .requestTimeoutMs(0)
                    .timeoutMs(150));
                fail("expected an overall-timeout SandboxException");
            } catch (SandboxException e) {
                assertTrue("expected the overall timeout named, got: " + e.getMessage(),
                    e.getMessage().contains("RunOptions.timeoutMs"));
                assertTrue("expected no start-event attribution, got: " + e.getMessage(),
                    !e.getMessage().contains("waiting for the start event"));
            }
        }
    }

    @Test(timeout = 15000)
    public void invalidFirstFrameFailsLoudly() throws Exception {
        // First frame is not a start event: runBackground must fail loudly
        // (the close-once finally path releases the stream and response).
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respond(exchange, dataFrame(stdoutData("hi"))))) {
            Commands commands = commandsFor(envd, 60_000L);
            try {
                commands.runBackground("x", new Commands.RunOptions());
                fail("expected an invalid-response SandboxException");
            } catch (SandboxException e) {
                assertTrue("expected invalid-response failure, got: " + e.getMessage(),
                    e.getMessage().contains("invalid response from server"));
            }
        }
    }

    @Test(timeout = 15000)
    public void connectRequestTimeoutOptionIsNamed() throws Exception {
        // connect() now takes per-call options like the official E2B
        // connect: the per-call requestTimeoutMs overrides the client-level
        // value and the timeout message names the option.
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.blockWithoutResponse(exchange))) {
            Commands commands = commandsFor(envd, 60_000L);
            try {
                commands.connect(42, new Commands.ConnectOptions().requestTimeoutMs(150));
                fail("expected a connect request-timeout SandboxException");
            } catch (SandboxException e) {
                assertTrue("expected connect-event timeout, got: " + e.getMessage(),
                    e.getMessage().contains("waiting for the connect event"));
                assertTrue("expected the per-call option named, got: " + e.getMessage(),
                    e.getMessage().contains("ConnectOptions.requestTimeoutMs"));
            }
        }
    }

    @Test(timeout = 15000)
    public void connectWatchdogTimesOut() throws Exception {
        // Without per-call options the client-level request timeout bounds
        // opening the stream (previously this blocked forever).
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.blockWithoutResponse(exchange))) {
            Commands commands = commandsFor(envd, 150L);
            try {
                commands.connect(42);
                fail("expected a connect request-timeout SandboxException");
            } catch (SandboxException e) {
                assertTrue("expected connect-event timeout, got: " + e.getMessage(),
                    e.getMessage().contains("waiting for the connect event"));
                assertTrue("expected the client-level fallback named, got: " + e.getMessage(),
                    e.getMessage().contains("client-level request timeout"));
            }
        }
    }

    @Test(timeout = 15000)
    public void connectPidMismatchFailsLoudly() throws Exception {
        // The first frame confirms a different PID: connect() must fail
        // loudly and release the stream and response (the close-once
        // finally path).
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respond(exchange, dataFrame(connectStartEvent(7))))) {
            Commands commands = commandsFor(envd, 60_000L);
            try {
                commands.connect(42);
                fail("expected a PID-mismatch SandboxException");
            } catch (SandboxException e) {
                assertTrue("expected PID-mismatch failure, got: " + e.getMessage(),
                    e.getMessage().contains("PID mismatch"));
            }
        }
    }

    @Test(timeout = 15000)
    public void connectTrailerErrorIsSurfaced() throws Exception {
        // The connect counterpart of the start path's first-frame trailer
        // check: a server error delivered in the connect stream's
        // end-of-stream trailer (before any data frame) must surface as
        // "Failed to connect to process: <server error>" instead of the
        // generic "no response from server".
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respond(exchange,
                errorTrailerFrame("{\"error\":{\"code\":\"internal\",\"message\":\"envd connect exploded\"}}")))) {
            Commands commands = commandsFor(envd, 60_000L);
            try {
                commands.connect(42, new Commands.ConnectOptions());
                fail("expected the trailer error to fail connect()");
            } catch (SandboxException e) {
                assertTrue("expected the extracted server message, not the raw trailer JSON, got: " + e.getMessage(),
                    e.getMessage().contains("envd connect exploded") && !e.getMessage().contains("\"error\""));
            }
        }
    }

    @Test(timeout = 15000)
    public void waitForCompletionOverallTimeoutAttributedToTimeoutMs() throws Exception {
        // Start event arrives, then the server goes silent: the overall
        // timeoutMs deadline (OkHttp call.timeout()) cuts the stream during
        // waitForCompletion, which must attribute it to the option via the
        // structural canceled-state detection.
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respondThenBlock(exchange, dataFrame(startEvent(42))))) {
            Commands commands = commandsFor(envd, 60_000L);
            CommandHandle handle = commands.runBackground("x", new Commands.RunOptions()
                .requestTimeoutMs(0)
                .timeoutMs(150));
            try {
                handle.waitForCompletion();
                fail("expected an overall-timeout SandboxException");
            } catch (SandboxException e) {
                assertTrue("expected the overall timeout named, got: " + e.getMessage(),
                    e.getMessage().contains("RunOptions.timeoutMs"));
            } finally {
                handle.close();
            }
        }
    }

    @Test(timeout = 15000)
    public void connectOverallTimeoutAttributedToTimeoutMs() throws Exception {
        // Start event arrives, then the server goes silent: the overall
        // ConnectOptions.timeoutMs deadline cuts the stream during
        // waitForCompletion, which must name the connect option (not the
        // start-path RunOptions.timeoutMs).
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respondThenBlock(exchange, dataFrame(connectStartEvent(42))))) {
            Commands commands = commandsFor(envd, 60_000L);
            CommandHandle handle = commands.connect(42, new Commands.ConnectOptions()
                .requestTimeoutMs(0)
                .timeoutMs(150));
            try {
                handle.waitForCompletion();
                fail("expected an overall-timeout SandboxException");
            } catch (SandboxException e) {
                assertTrue("expected the connect option named, got: " + e.getMessage(),
                    e.getMessage().contains("ConnectOptions.timeoutMs"));
                assertTrue("expected no start-path attribution, got: " + e.getMessage(),
                    !e.getMessage().contains("RunOptions.timeoutMs"));
            } finally {
                handle.close();
            }
        }
    }

    @Test(timeout = 15000)
    public void disconnectFailsWaitWithoutKilling() throws Exception {
        // Command keeps running (no end event); disconnect() drops the
        // stream (the official E2B operation name), waitForCompletion()
        // fails with the disconnected message, and no SendSignal is ever
        // issued — killing is an explicit operation.
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respondThenBlock(exchange, dataFrame(startEvent(42))))) {
            Commands commands = commandsFor(envd, 60_000L);
            CommandHandle handle = commands.runBackground("sleep 60", new Commands.RunOptions());

            handle.disconnect();
            try {
                handle.waitForCompletion();
                fail("expected the disconnected stream to fail waitForCompletion()");
            } catch (SandboxException e) {
                assertTrue("expected the disconnected message, got: " + e.getMessage(),
                    e.getMessage().contains("disconnected"));
            }
            // Sticky too: the second call replays the disconnected failure.
            try {
                handle.waitForCompletion();
                fail("expected the second call to replay the disconnected failure");
            } catch (SandboxException e) {
                assertTrue("expected the disconnected message again, got: " + e.getMessage(),
                    e.getMessage().contains("disconnected"));
            }
            assertFalse("disconnect() must not issue a kill RPC",
                envd.receivedKillRequest());
        }
    }

    @Test(timeout = 15000)
    public void trailerErrorIsSurfaced() throws Exception {
        // A mid-stream server error travels in the Connect Protocol
        // end-of-stream trailer; it must surface instead of the misleading
        // "no end event" default.
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respond(exchange,
                dataFrame(startEvent(42)),
                errorTrailerFrame("{\"error\":{\"code\":\"internal\",\"message\":\"envd exploded\"}}")))) {
            Commands commands = commandsFor(envd, 60_000L);
            CommandHandle handle = commands.runBackground("x", new Commands.RunOptions());
            try {
                handle.waitForCompletion();
                fail("expected the trailer error to fail waitForCompletion()");
            } catch (SandboxException e) {
                assertTrue("expected the extracted server message, not the raw trailer JSON, got: " + e.getMessage(),
                    e.getMessage().contains("envd exploded") && !e.getMessage().contains("\"error\""));
            }
        }
    }

    @Test
    public void zeroOverallTimeoutMeansNoLimitOnBothOptionTypes() {
        // The official timeout-to-ms treats 0 as unlimited, and the Go side
        // likewise reads a zero RunOpts.Timeout / ConnectOpts.Timeout as no
        // limit; both option classes accept 0 and only reject negatives,
        // matching the requestTimeoutMs validation next to them.
        assertEquals(0L, new Commands.RunOptions().timeoutMs(0).getTimeoutMs().longValue());
        assertEquals(0L, new Commands.ConnectOptions().timeoutMs(0).getTimeoutMs().longValue());
        try {
            new Commands.RunOptions().timeoutMs(-1);
            fail("expected a negative timeoutMs to be rejected");
        } catch (IllegalArgumentException expected) {
        }
        try {
            new Commands.ConnectOptions().timeoutMs(-1);
            fail("expected a negative timeoutMs to be rejected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test(timeout = 15000)
    public void secondKillReturnsFalse() throws Exception {
        // kill() claims completion atomically (state lock, check-then-act
        // replaced): a second call reports "already completed" — previously
        // two concurrent kills could both return true.
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respondThenBlock(exchange, dataFrame(startEvent(42))))) {
            Commands commands = commandsFor(envd, 60_000L);
            CommandHandle handle = commands.runBackground("sleep 60", new Commands.RunOptions());
            assertTrue("first kill() must claim the handle", handle.kill());
            assertFalse("second kill() must report already-completed", handle.kill());
            assertEquals(-1, handle.waitForCompletion().getExitCode());
        }
    }

    @Test(timeout = 15000)
    public void failedKillRollsBackCompletionClaim() throws Exception {
        // The kill RPC runs outside the state lock; when it throws, the
        // completion claim is rolled back so the kill can be retried
        // (previously the claim would have stuck, making a later
        // waitForCompletion() return the synthetic -1 even though no kill
        // ever reached the sandbox).
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respondThenBlock(exchange, dataFrame(startEvent(42))))) {
            Commands commands = commandsFor(envd, 60_000L);
            CommandHandle handle = commands.runBackground("sleep 60", new Commands.RunOptions());
            envd.failSignal = true;
            try {
                handle.kill();
                fail("expected the failed kill RPC to propagate");
            } catch (RuntimeException expected) {
            }
            envd.failSignal = false;
            assertTrue("after the rollback a retrying kill() must claim the handle",
                handle.kill());
            assertEquals(-1, handle.waitForCompletion().getExitCode());
        }
    }

    @Test(timeout = 15000)
    public void killNotFoundSurfacesAndRollsBack() throws Exception {
        // A 404 from SendSignal means no process was there to signal —
        // previously the boolean was discarded and kill() reported true,
        // claiming completion with no kill delivered (and a wait on a
        // silent stream then stayed blocked forever). The kill action now
        // surfaces it, and the handle's kill() rolls the claim back so the
        // kill can be retried — mirroring the Go handleKill's real
        // boolean.
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respondThenBlock(exchange, dataFrame(startEvent(42))))) {
            Commands commands = commandsFor(envd, 60_000L);
            CommandHandle handle = commands.runBackground("sleep 60", new Commands.RunOptions());
            envd.signalNotFound = true;
            try {
                handle.kill();
                fail("expected the 404 kill to surface");
            } catch (IllegalStateException e) {
                assertTrue("expected the no-signal message, got: " + e.getMessage(),
                    e.getMessage().contains("no signal was sent"));
            }
            assertFalse("the claim must roll back when no signal was sent",
                handle.isCompleted());
        }
    }

    @Test(timeout = 15000)
    public void killDuringBlockedWaitReturnsSyntheticResult() throws Exception {
        // The concurrency gap the state lock alone did not cover: kill()
        // claims completion while a wait is blocked in the read loop. The
        // wait must return the synthetic -1 via the killed flag — not throw
        // the misleading sticky "no end event" failure, which also meant a
        // later real end event was never read.
        //
        // No assertion here depends on a thread interleaving or on a sleep.
        // processResponse appends to stdout before invoking the onStdout
        // callback, so gating kill() on the callback of the last pre-kill
        // frame proves that frame is already collected and that the waiter
        // is inside the read loop (not on the entry fast path). Which branch
        // the loop then leaves by — breaking on the claim at that frame's
        // post-check, reading a post-kill frame first, or finding the stream
        // ended — is a nanosecond window no latch can order, and all of them
        // must yield the same synthetic result. The stream ends with no end
        // event, so the real exit code can never win against the claim.
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respondAfterKillSignal(exchange,
                new byte[][]{dataFrame(startEvent(42)), dataFrame(stdoutData("in-loop")),
                    dataFrame(stdoutData("pre-kill"))},
                dataFrame(stdoutData("post-kill"))))) {
            Commands commands = commandsFor(envd, 60_000L);
            // Counted down from the stdout callback of the last pre-kill
            // frame: by then that frame is in the stdout buffer and the
            // waiter is provably inside the read loop.
            final CountDownLatch waiterInLoop = new CountDownLatch(1);
            CommandHandle handle = commands.runBackground("sleep 60", new Commands.RunOptions()
                .onStdout(s -> { if (s.contains("pre-kill")) { waiterInLoop.countDown(); } }));

            final AtomicReference<CommandResult> waiterResult = new AtomicReference<>();
            final AtomicReference<Exception> waiterError = new AtomicReference<>();
            Thread waiter = new Thread(() -> {
                try {
                    waiterResult.set(handle.waitForCompletion());
                } catch (Exception e) {
                    waiterError.set(e);
                }
            });
            waiter.start();

            assertTrue("waiter never reached the read loop", waiterInLoop.await(5, java.util.concurrent.TimeUnit.SECONDS));
            // No sleep between the latch and the claim: everything asserted
            // below holds whether or not the claim beats the waiter's next
            // stateLock check.
            assertTrue("kill() must claim the handle while the wait is blocked", handle.kill());
            waiter.join(5000);

            assertNull("the blocked wait must not throw on a kill claim, got: " + waiterError.get(),
                waiterError.get());
            assertNotNull("the blocked wait must return a result", waiterResult.get());
            assertEquals("a wait unblocked by the kill claim returns the synthetic code",
                -1, waiterResult.get().getExitCode());
            assertTrue("frames consumed before the claim are kept",
                waiterResult.get().getStdout().contains("in-loop")
                    && waiterResult.get().getStdout().contains("pre-kill"));
            // No sticky failure was recorded — the same synthetic result
            // replays on the next call instead of an exception.
            assertEquals(-1, handle.waitForCompletion().getExitCode());
        }
    }

    /** Blocks until the waiter thread is parked inside
     * ConnectStreamReader.hasNext() — past every completion check the read
     * loop makes, waiting for a frame that cannot arrive until the test
     * releases it. A stdout callback cannot establish this: it fires inside
     * processResponse, before the loop's post-frame check, so a claim
     * landing in that window makes the waiter break on the claim and skip
     * the staged frames — a legal outcome, but not what a test about the
     * blocked-wait path may assert on. Matching our own class and method
     * (not a JDK frame) keeps this a real happens-before rather than a
     * timing margin. Bounded: a waiter that never parks must fail the
     * assertions, not hang the test. */
    private static void awaitParkedInHasNext(Thread waiter) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5_000L;
        while (System.currentTimeMillis() < deadline) {
            for (StackTraceElement frame : waiter.getStackTrace()) {
                if ("hasNext".equals(frame.getMethodName())
                    && frame.getClassName().endsWith("ConnectStreamReader")) {
                    return;
                }
            }
            Thread.sleep(1);
        }
        fail("waiter never parked inside ConnectStreamReader.hasNext(); last stack: "
            + java.util.Arrays.toString(waiter.getStackTrace()));
    }

    @Test(timeout = 15000)
    public void failedKillLeavesBlockedWaitReadingTheRealEndEvent() throws Exception {
        // The rollback side of the kill claim, with the waiter parked inside
        // hasNext() before the claim is made (failedKillRollsBackCompletionClaim
        // is sequential): the failed kill RPC rolls killed/completed back, so
        // the waiter must keep reading — collecting the late data frame and
        // the real end event (exit code 137) — instead of stranding on a
        // claim that no longer exists, returning the synthetic -1, or
        // throwing the sticky "no end event" failure. The late frames are
        // released by a test-owned latch counted down after kill() throws
        // (which happens after the rollback), staging the order
        // deterministically; the server-side kill-arrival latch alone could
        // not, since the 500 response races the late frames back to the
        // client.
        final CountDownLatch rollbackDone = new CountDownLatch(1);
        try (MockEnvd envd = MockEnvd.start((server, exchange, path) ->
            server.respondAfterLatch(exchange, rollbackDone,
                new byte[][]{dataFrame(startEvent(42)), dataFrame(stdoutData("in-loop"))},
                dataFrame(stdoutData("late-output")),
                dataFrame(endEvent(137))))) {
            Commands commands = commandsFor(envd, 60_000L);
            final CountDownLatch waiterInLoop = new CountDownLatch(1);
            CommandHandle handle = commands.runBackground("sleep 60", new Commands.RunOptions()
                .onStdout(s -> { if (s.contains("in-loop")) { waiterInLoop.countDown(); } }));

            final AtomicReference<CommandResult> waiterResult = new AtomicReference<>();
            final AtomicReference<Exception> waiterError = new AtomicReference<>();
            Thread waiter = new Thread(() -> {
                try {
                    waiterResult.set(handle.waitForCompletion());
                } catch (Exception e) {
                    waiterError.set(e);
                }
            });
            waiter.start();

            assertTrue("waiter never reached the read loop", waiterInLoop.await(5, java.util.concurrent.TimeUnit.SECONDS));
            // The callback above only proves the frame was consumed; park the
            // waiter in hasNext() so the claim cannot land inside the window
            // before its post-frame check. Nothing can arrive until
            // rollbackDone is released, so from here the order is forced.
            awaitParkedInHasNext(waiter);
            envd.failSignal = true;
            try {
                handle.kill();
                fail("expected the failed kill RPC to propagate");
            } catch (RuntimeException expected) {
            }
            // kill() rethrows only after the rollback, so this release is
            // ordered after it.
            rollbackDone.countDown();
            waiter.join(5000);

            assertNull("the waiter must not return a result after the rollback, got: " + waiterResult.get(),
                waiterResult.get());
            assertTrue("the waiter must report the real end event, got: " + waiterError.get(),
                waiterError.get() instanceof CommandExitException);
            CommandExitException exit = (CommandExitException) waiterError.get();
            assertEquals("the real exit code survived the rolled-back claim",
                137, exit.getExitCode());
            assertTrue("the late data frame was collected after the rollback",
                exit.getStdout().contains("late-output"));
            // The real end event completed the handle, so a retrying kill()
            // is a no-op — the real data wins over the rolled-back claim.
            assertFalse("a kill after the real end event must not claim the handle",
                handle.kill());
        }
    }

    private static String assertWaitThrows(CommandHandle handle) {
        try {
            handle.waitForCompletion();
            fail("expected waitForCompletion() to fail");
        } catch (SandboxException e) {
            return e.getMessage();
        }
        throw new AssertionError("unreachable");
    }
}
