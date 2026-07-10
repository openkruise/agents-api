package io.openkruise.agents.client.runtime.commands;

import io.openkruise.agents.client.runtime.envd.process.StartResponse;
import io.openkruise.agents.client.runtime.envd.process.ProcessEvent;
import io.openkruise.agents.client.runtime.exceptions.SandboxException;
import io.openkruise.agents.client.runtime.utils.MessageStream;
import okhttp3.Response;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
    private final StringBuilder stdout = new StringBuilder();
    private final StringBuilder stderr = new StringBuilder();
    private final AtomicInteger exitCode = new AtomicInteger(-1);
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

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
        if (pid <= 0) {
            throw new IllegalArgumentException("PID must be positive, got: " + pid);
        }
        this.pid = pid;
        this.streamReader = streamReader;
        this.streamingResponse = streamingResponse;
        this.killAction = killAction;
        this.onStdout = onStdout;
        this.onStderr = onStderr;
    }

    public long getPid() {
        return pid;
    }

    public boolean isCompleted() {
        return completed.get();
    }

    /**
     * Blocks until the command execution completes and returns the result.
     */
    public CommandResult waitForCompletion() {
        if (completed.get()) {
            return createResult();
        }

        try {
            while (streamReader != null && streamReader.hasNext()) {
                StartResponse response = streamReader.next();
                processResponse(response);

                if (completed.get()) {
                    break;
                }
            }
        } catch (Exception e) {
            throw new SandboxException("Error while waiting for command completion", e);
        } finally {
            synchronized (this) {
                completed.set(true);
                close();
            }
        }

        return createResult();
    }

    private CommandResult createResult() {
        return new CommandResult(stdout.toString(), stderr.toString(), exitCode.get());
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
            exitCode.set(endEvent.getExitCode());
            completed.set(true);
        }
    }

    /**
     * Terminates the command (SIGKILL).
     */
    public boolean kill() {
        if (completed.get()) {
            return false;
        }
        if (killAction != null) {
            killAction.run();
        }
        exitCode.set(-1);
        completed.set(true);
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
