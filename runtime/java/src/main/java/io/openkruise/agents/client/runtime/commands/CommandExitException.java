package io.openkruise.agents.client.runtime.commands;

import io.openkruise.agents.client.runtime.exceptions.SandboxException;

/**
 * Thrown by {@link CommandHandle#waitForCompletion()} when the command
 * finished with a non-zero exit code — the official E2B SDK contract
 * (Python {@code CommandExitException}, JS {@code CommandExitError}, and
 * this repository's Go {@code CommandExitError} all throw on non-zero).
 * <p>
 * The full result travels with the exception: use the getters to read the
 * exit code, stdout, stderr and the server-side error message (when envd
 * delivered one in the end event) instead of losing them in the error path.
 */
public class CommandExitException extends SandboxException {
    private final CommandResult result;

    public CommandExitException(CommandResult result) {
        super(buildMessage(result));
        this.result = result;
    }

    private static String buildMessage(CommandResult result) {
        // Mirrors the Go CommandExitError shape: "command exited with code
        // X: <error>" with the server-side error (when present) ahead of
        // the stderr trailer.
        String msg = "command exited with code " + result.getExitCode();
        if (!result.getError().isEmpty()) {
            msg += ": " + result.getError();
        }
        if (!result.getStderr().isEmpty()) {
            msg += "\nstderr: " + result.getStderr();
        }
        return msg;
    }

    public int getExitCode() {
        return result.getExitCode();
    }

    public String getStdout() {
        return result.getStdout();
    }

    public String getStderr() {
        return result.getStderr();
    }

    public String getError() {
        return result.getError();
    }
}
