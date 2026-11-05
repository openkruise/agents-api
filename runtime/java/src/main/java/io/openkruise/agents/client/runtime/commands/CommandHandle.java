package io.openkruise.agents.client.runtime.commands;

import io.openkruise.agents.client.runtime.envd.process.StartResponse;
import io.openkruise.agents.client.runtime.envd.process.ProcessEvent;
import io.openkruise.agents.client.runtime.exceptions.SandboxException;
import io.openkruise.agents.client.runtime.utils.MessageStream;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * CommandHandle represents a handle to a running command.
 * <p>
 * Reads streaming events from OkHttp Response via Connect Protocol Server-Streaming.
 * Provides {@link #waitForCompletion()} to block until command completes, and {@link #kill()} to terminate the command.
 */
public class CommandHandle implements AutoCloseable {
    private final long pid;
    private final MessageStream<StartResponse> streamReader;
    private final Response streamingResponse;
    private final Runnable killAction;
    /** Cancels the underlying HTTP call without killing the process. */
    private final Runnable cancelAction;
    private final StringBuilder stdout = new StringBuilder();
    private final StringBuilder stderr = new StringBuilder();
    private final AtomicInteger exitCode = new AtomicInteger(-1);
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    /** Set when the user called disconnect(): the stream was dropped on
     * purpose, which waitForCompletion reports distinctly from a network
     * failure. */
    private final AtomicBoolean disconnected = new AtomicBoolean(false);
    /** Set when an end event arrived, distinguishing a clean finish from a
     * stream that ended without one (disconnect, cancel, IO failure). */
    private volatile boolean sawEndEvent;
    /** Set (under stateLock) by kill() just before it claims completion.
     * Distinguishes a local kill from a stream failure in the blocked
     * wait's post-loop branch: the read loop breaks on the claim
     * mid-stream (the next frame was not necessarily the end event), and
     * the outcome is the synthetic exit code -1 — not the misleading
     * "no end event" failure, which would also turn sticky and discard
     * any late end event. Cleared by the failed-kill rollback. */
    private volatile boolean killed;
    /** Set once the kill RPC settled successfully (or there is no
     * killAction at all), making the claim permanent — no rollback path
     * can run afterwards. Read by the entry fast path of
     * waitForCompletion() to decide whether closing the output stream is
     * safe: before the RPC settles, a failure would roll the claim back
     * and the stream must stay readable for a retry. */
    private volatile boolean killConfirmed;
    /** The server-side error message delivered in the end event, when one
     * was present (envd reports e.g. spawn failures there); empty when the
     * process exited normally. Carried into the CommandResult like the
     * official SDKs (Python CommandResult.error, Go CommandResult.Error). */
    private volatile String endError = "";
    /** The failure reported by a previous waitForCompletion() call when the
     * stream ended without an end event (cancel, timeout, IO error, server
     * trailer). Replayed on every subsequent call so a failed handle never
     * silently returns a partial result; completed stays false so kill()
     * remains usable (the process may still be running in the sandbox). */
    private volatile SandboxException failure;
    /** Per-call overall stream timeout (RunOptions.timeoutMs on the start
     * path, ConnectOptions.timeoutMs on the connect path) when one was
     * armed on the call via OkHttp call.timeout(); null otherwise. Lets
     * waitForCompletion attribute a stream cut by that timeout to the
     * option instead of a generic IO failure (parity with Go's
     * RunOpts.Timeout / ConnectOpts.Timeout attribution). */
    private volatile Long timeoutMs;
    /** The user-facing option name that armed timeoutMs ("RunOptions.timeoutMs"
     * or "ConnectOptions.timeoutMs"); defaults to the start-path name for
     * handles constructed directly. */
    private volatile String timeoutSource = "RunOptions.timeoutMs";
    /** Serializes the completed/exitCode/sawEndEvent transition between the
     * reader thread (the end-event branch of processResponse) and kill():
     * without it, a kill() landing between the end event's exitCode.set and
     * completed.set overwrites the real exit code with the synthetic -1.
     * Held only for plain memory writes — never across I/O — so it cannot
     * block the reader thread beyond nanoseconds. */
    private final Object stateLock = new Object();
    /** Reports whether the underlying OkHttp call was canceled (set by
     * Commands from call::isCanceled). OkHttp fires call.timeout() by
     * canceling the call, so "canceled && not user-canceled" is a
     * structural signal of the per-call timeout — used instead of matching
     * error text, which a genuine network error message could coincidentally
     * match. Null for handles constructed directly. */
    private volatile BooleanSupplier callCanceled;

    private final Consumer<String> onStdout;
    private final Consumer<String> onStderr;

    /**
     * @param pid               Process PID
     * @param streamReader      Message stream reader (ConnectStreamReader or adapter)
     * @param streamingResponse OkHttp Response to close when handle is closed
     * @param killAction        Callback to terminate the command (calls Commands.kill)
     * @param onStdout          stdout callback
     * @param onStderr          stderr callback
     */
    public CommandHandle(long pid, MessageStream<StartResponse> streamReader,
                         Response streamingResponse,
                         Runnable killAction,
                         Consumer<String> onStdout, Consumer<String> onStderr) {
        this(pid, streamReader, streamingResponse, killAction, null, onStdout, onStderr);
    }

    /**
     * @param pid               Process PID
     * @param streamReader      Message stream reader (ConnectStreamReader or adapter)
     * @param streamingResponse OkHttp Response to close when handle is closed
     * @param killAction        Callback to terminate the command (calls Commands.kill)
     * @param cancelAction      Callback to cancel the underlying HTTP call
     *                          without killing the process (may be null)
     * @param onStdout          stdout callback
     * @param onStderr          stderr callback
     */
    public CommandHandle(long pid, MessageStream<StartResponse> streamReader,
                         Response streamingResponse,
                         Runnable killAction,
                         Runnable cancelAction,
                         Consumer<String> onStdout, Consumer<String> onStderr) {
        if (pid <= 0) {
            throw new IllegalArgumentException("PID must be positive, got: " + pid);
        }
        this.pid = pid;
        this.streamReader = streamReader;
        this.streamingResponse = streamingResponse;
        this.killAction = killAction;
        this.cancelAction = cancelAction;
        this.onStdout = onStdout;
        this.onStderr = onStderr;
    }

    public long getPid() {
        return pid;
    }

    /** Records the per-call overall stream timeout (RunOptions.timeoutMs /
     * ConnectOptions.timeoutMs) when one was armed on the underlying call,
     * for error attribution in waitForCompletion. source is the user-facing
     * option name. Package-private: set by Commands, which knows whether a
     * call timeout was configured and which option armed it. */
    CommandHandle withTimeoutMs(Long timeoutMs, String source) {
        this.timeoutMs = (timeoutMs != null && timeoutMs > 0) ? timeoutMs : null;
        if (source != null) {
            this.timeoutSource = source;
        }
        return this;
    }

    /** Records a supplier reporting whether the underlying OkHttp call is
     * canceled, for structural per-call-timeout detection in
     * waitForCompletion. Package-private: set by Commands, which owns the
     * Call reference. */
    CommandHandle withCallCanceled(BooleanSupplier callCanceled) {
        this.callCanceled = callCanceled;
        return this;
    }

    public boolean isCompleted() {
        return completed.get();
    }

    /**
     * Blocks until the command execution completes and returns the result.
     * <p>
     * Concurrency contract: one waiting thread at a time — the read loop
     * owns the underlying stream, so concurrent waitForCompletion() calls
     * on the same handle are unsupported. kill() and disconnect() may be
     * called from any thread while a wait is blocked; see their javadoc
     * for the resulting semantics.
     * <p>
     * If the stream ends without an end event — dropped via
     * {@link #disconnect()}, interrupted by an IO failure, or closed by the
     * server — a {@link SandboxException} is thrown instead of silently
     * returning a partial result. A local {@link #kill()} is the one
     * exception: when its completion claim unblocks a wait that then
     * exits without processing an end event — whether on a non-end frame
     * or on stream end — the wait returns the synthetic exit code
     * -1 (see kill()).
     * If the command finishes with a non-zero
     * exit code, a {@link CommandExitException} carrying the full result
     * (stdout, stderr, exit code) is thrown — the official E2B SDK
     * contract (Python CommandExitException, JS CommandExitError; the Go
     * SDK of this repository returns a CommandExitError).
     * <p>
     * A failed stream is sticky: every subsequent call replays the same
     * exception, and {@link #kill()} stays usable because the process may
     * still be running in the sandbox.
     */
    public CommandResult waitForCompletion() {
        // A recorded failure wins over completed: kill() after a failed
        // wait sets completed, and answering with a synthetic exit-code -1
        // result would swallow the real stream failure. The official SDKs
        // likewise let the iteration error take priority over any result.
        if (failure != null) {
            throw failure;
        }
        if (completed.get()) {
            // Release the output stream when the claim is permanent: a
            // kill-then-wait never enters the read loop (whose finally
            // would close it), leaving the OkHttp connection open until
            // GC otherwise. While the kill RPC is still in flight the
            // fast path leaves the stream open — a failed RPC rolls the
            // claim back and the kill can be retried; the handle stays
            // readable only if no concurrent waiter has closed the stream
            // in the meantime (see the kill() javadoc).
            // sawEndEvent means the read loop already closed it; close()
            // is CAS-guarded, so this is idempotent.
            if (sawEndEvent || killConfirmed) {
                close();
            }
            return createResult();
        }

        // Whether a kill claim unblocked this wait, snapshotted under
        // stateLock when the read loop left it. A failed kill RPC rolls the
        // claim (and killed) back afterwards, but a wait the claim already
        // unblocked must not be retroactively turned into a stream failure —
        // the snapshot freezes the claim's intent at the moment the loop
        // broke.
        boolean unblockedByKill = false;
        try {
            while (streamReader != null && streamReader.hasNext()) {
                StartResponse response = streamReader.next();
                processResponse(response);

                // Atomic check-and-snapshot: checked outside the lock, a
                // concurrent rollback could clear completed between the
                // check and the read of killed, leaving the loop broken
                // with the claim already erased.
                synchronized (stateLock) {
                    if (completed.get()) {
                        unblockedByKill = killed;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw fail(new SandboxException("Error while waiting for command completion", e));
        } finally {
            // Note: completed is NOT set here. It is set only by a real end
            // event (processResponse) or by kill(); marking a failed stream
            // complete would make a second call silently return a partial
            // result and make kill() a no-op — contradicting the fail-loudly
            // contract above. close() is CAS-guarded, so a double close with
            // a concurrent fast-path release is safe.
            close();
        }

        if (!unblockedByKill) {
            // The loop exited on stream end rather than on the claim (the
            // wait was blocked inside hasNext() when the claim landed, and
            // the stream ended before another frame arrived). A claim
            // still active at this observation point also unblocked the
            // wait; read both flags under the lock so a concurrent
            // rollback cannot split them.
            synchronized (stateLock) {
                unblockedByKill = killed && completed.get();
            }
        }

        if (!sawEndEvent) {
            // A local kill unblocked this wait — either the loop broke on
            // the completion claim mid-stream, or the stream ended while
            // the claim was active. That is not a stream failure — the
            // caller asked for this outcome — so return the synthetic
            // result (exit code -1) instead of throwing the misleading
            // "no end event" error, and record no sticky failure, matching
            // the entry fast path.
            if (unblockedByKill) {
                return createResult();
            }
            if (disconnected.get()) {
                throw fail(new SandboxException(
                    "Command stream disconnected; the process may still be running in the sandbox"));
            }
            IOException lastError = streamReader != null ? streamReader.getLastError() : null;
            if (lastError != null) {
                // By this point the request watchdog is disarmed, so a
                // canceled call can only be the per-call call timeout (user
                // cancels are filtered out by the canceled flag above).
                // Attribute it to the option name like the Go side does for
                // RunOpts.Timeout.
                if (timeoutMs != null && isCallTimeout()) {
                    throw fail(new SandboxException("Timed out after " + timeoutMs
                        + "ms (" + timeoutSource + "; the process may still be running in the sandbox)", lastError));
                }
                String message = lastError.getMessage();
                throw fail(new SandboxException("Command stream failed: "
                    + (message != null ? message : lastError.getClass().getSimpleName()), lastError));
            }
            // A mid-stream server error travels in the Connect Protocol
            // end-of-stream trailer rather than an IO error; surface it
            // instead of the misleading "no end event" default.
            String trailerError = streamReader != null ? streamReader.getTrailerError() : null;
            if (trailerError != null) {
                throw fail(new SandboxException("Command stream failed: " + trailerError));
            }
            throw fail(new SandboxException("Command ended without an end event"));
        }

        CommandResult result = createResult();
        if (result.getExitCode() != 0) {
            // Official E2B contract (Python CommandExitException, JS
            // CommandExitError, this repo's Go CommandExitError): a
            // non-zero exit code is an error, not a result. fail() makes it
            // sticky so a second waitForCompletion() replays the same
            // exception instead of silently returning the result.
            throw fail(new CommandExitException(result));
        }
        return result;
    }

    /** Records a stream failure or non-zero exit as sticky (see
     * {@link #failure}) and returns the exception for the caller to throw,
     * so a later waitForCompletion() replays it instead of returning a
     * partial or non-zero result. */
    private SandboxException fail(SandboxException e) {
        failure = e;
        return e;
    }

    private CommandResult createResult() {
        return new CommandResult(stdout.toString(), stderr.toString(), exitCode.get(), endError);
    }

    /** A stream failure caused by the per-call call timeout: the call was
     * canceled (OkHttp fires call.timeout() by canceling the call) while
     * the user's disconnect() flag is unset — by this point the request
     * watchdog is disarmed, so nothing else cancels the call. Structural
     * detection only: timeoutMs is set exclusively by Commands, which
     * always pairs it with the callCanceled supplier, so the supplier is
     * never null on this path (defensively false if it ever is). */
    private boolean isCallTimeout() {
        if (disconnected.get()) {
            return false;
        }
        return callCanceled != null && callCanceled.getAsBoolean();
    }

    private void processResponse(StartResponse response) {
        if (!response.hasEvent()) {
            return;
        }

        ProcessEvent event = response.getEvent();

        if (event.hasData()) {
            ProcessEvent.DataEvent dataEvent = event.getData();

            if (dataEvent.hasStdout()) {
                String out = dataEvent.getStdout().toStringUtf8();
                synchronized (stdout) {
                    stdout.append(out);
                }
                if (onStdout != null) {
                    onStdout.accept(out);
                }
            }

            if (dataEvent.hasStderr()) {
                String err = dataEvent.getStderr().toStringUtf8();
                synchronized (stderr) {
                    stderr.append(err);
                }
                if (onStderr != null) {
                    onStderr.accept(err);
                }
            }
        }

        if (event.hasEnd()) {
            ProcessEvent.EndEvent endEvent = event.getEnd();
            synchronized (stateLock) {
                // Real end-event data is authoritative: even when kill()
                // claimed completion first (leaving the synthetic -1), the
                // real exit code overwrites it, and sawEndEvent keeps
                // waitForCompletion() on the normal-completion path instead
                // of the synthetic early return.
                exitCode.set(endEvent.getExitCode());
                if (endEvent.hasError()) {
                    endError = endEvent.getError();
                }
                sawEndEvent = true;
                completed.set(true);
            }
        }
    }

    /**
     * Stops reading the command output without killing the process — the
     * official E2B SDK's disconnect() operation (the Go SDK of this
     * repository names it Disconnect()). A blocked
     * {@link #waitForCompletion()} fails with a {@link SandboxException},
     * while the process keeps running in the sandbox (terminate it
     * explicitly via {@link #kill()} if needed). Functionally equivalent
     * to the official SDK's per-call AbortSignal cancellation, which the
     * official handle exposes as disconnect().
     */
    public void disconnect() {
        disconnected.set(true);
        if (cancelAction != null) {
            cancelAction.run();
        }
        close();
    }

    /**
     * Terminates the command (SIGKILL) and claims completion. The claim
     * does not cancel the output stream: a {@link #waitForCompletion()}
     * call already blocked on it is released the next time its read loop
     * checks the claim — after processing whatever frame arrives next, or
     * when the stream ends. Such a wait returns the synthetic exit code
     * -1 whenever it exits on the completion claim without having
     * processed an end event: either it broke on a non-end frame (e.g. a
     * data frame) while the claim was active — in which case a later end
     * event is never read, because the stream is closed on exit — or the
     * stream ended with no end event. A real end event (e.g. exit code
     * 137) wins only when it is actually processed before the stream is
     * released; it is then reported like any other end event (a non-zero
     * code still throws a {@link CommandExitException}). If the stream
     * never yields another frame after a successful kill — envd stall or
     * crash, or a dropped connection that has not timed out — the blocked
     * wait stays blocked; bound it with
     * RunOptions/ConnectOptions timeoutMs if that matters.
     * A waitForCompletion() call made only after kill() returns
     * immediately with the synthetic exit code -1 from the entry fast
     * path, without waiting for envd's end event; that early return
     * bypasses the non-zero-exit check, so only a real end event (not
     * the synthetic kill code) triggers the exception. The fast path
     * also releases the output stream once the kill RPC has settled
     * (a permanent claim can no longer be rolled back; before that the
     * fast path leaves the stream open, so a kill that fails with no
     * concurrent waiter reading keeps the handle readable for a retry).
     * <p>
     * Concurrency: safe to call from any thread, including while a wait is
     * blocked. The completion claim, the killed flag and the synthetic -1
     * are written under the state lock shared with the end-event branch,
     * so a kill racing the final end event resolves deterministically:
     * if the end event is processed first, kill() returns false and the
     * real exit code is kept, never overwritten by the synthetic -1; if
     * the kill claims first, a real end event still processed before the
     * stream is released overwrites the synthetic code ("real data
     * wins"). When the claim wins and the read loop's next frame is not
     * the end event, a stateLock-consistent snapshot of the killed flag
     * — taken when the wait leaves the read loop — tells its post-loop
     * branch that this was a local kill, not a stream failure: the wait
     * returns the synthetic -1 instead of throwing the misleading "no
     * end event" error. The kill RPC runs
     * outside the lock; if it throws — a transport failure, or the
     * process being gone so no signal was sent (HTTP 404, surfaced like
     * the Go handleKill's real boolean) — the claim is rolled back
     * (unless a real end event completed it meanwhile — its data wins).
     * The rollback restores the completion state so the kill RPC can be
     * retried, but it does not reopen the output stream: a concurrent
     * waiter may have consumed a data frame while the RPC was in flight,
     * observed the claim, and closed the stream in its finally block
     * before the failure. After such a rollback, retrying kill() is
     * still possible, but a fresh waitForCompletion() is not guaranteed
     * to be usable — the stream it would read may already be released.
     * Only one of N concurrent kill() calls returns true; kill() returns
     * true only when the signal was sent.
     *
     * @return true when the signal was sent; false when the command had
     *         already completed
     * @throws IllegalStateException if envd reports the process is gone
     *         (HTTP 404): no signal was sent and the completion claim is
     *         rolled back so kill() can be retried. The rollback does not
     *         reopen the output stream — a concurrent waiter may already
     *         have released it — so a fresh waitForCompletion() is not
     *         guaranteed to be usable. Callers that release the handle
     *         manually should prefer try-with-resources (or a finally
     *         block) so the stream is still closed when this throws.
     */
    public boolean kill() {
        synchronized (stateLock) {
            if (completed.get()) {
                return false;
            }
            // Claim completion under the lock so the transition is atomic
            // with the end-event branch; killed first, so the blocked wait
            // can never observe the claim without the flag. The synthetic
            // code is written only when no end event has recorded the real
            // one; a concurrent early-return from waitForCompletion() may
            // observe the claim before this write, but exitCode's initial
            // value is already the synthetic -1, so it reads the same
            // value either way.
            killed = true;
            completed.set(true);
            if (!sawEndEvent) {
                exitCode.set(-1);
            }
        }
        if (killAction != null) {
            try {
                killAction.run();
            } catch (RuntimeException | Error e) {
                // Roll the claim back so a failed kill can be retried; the
                // rollback does not reopen the output stream (a concurrent
                // waiter may already have released it), so a fresh
                // waitForCompletion() is not guaranteed to be usable — see
                // the kill() javadoc. When a real end event completed the
                // handle meanwhile, its data wins and the claim stays.
                // (exitCode keeps the synthetic -1 on this path — identical
                // to its initial value, so nothing observable changes.)
                // Errors roll back too: an Error escaping the kill action
                // must not strand the claim with no kill delivered.
                synchronized (stateLock) {
                    if (!sawEndEvent) {
                        completed.set(false);
                        killed = false;
                    }
                }
                throw e;
            }
        }
        // The claim is permanent now — nothing can roll it back anymore,
        // so the entry fast path of waitForCompletion() may close the
        // stream (see killConfirmed). Written under stateLock so a
        // concurrent waitForCompletion() entering the fast path after
        // completed.get() == true is guaranteed to observe the flag.
        synchronized (stateLock) {
            killConfirmed = true;
        }
        return true;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            if (streamReader != null) {
                try {
                    streamReader.close();
                } catch (Exception e) {
                    // Resource cleanup, ignore close exceptions
                }
            }
            if (streamingResponse != null) {
                try {
                    streamingResponse.close();
                } catch (Exception ignored) {
                    // Already closed by streamReader (ConnectResponseAdapter), ignore double-close
                }
            }
        }
    }
}
