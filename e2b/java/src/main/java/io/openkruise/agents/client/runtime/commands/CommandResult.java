package io.openkruise.agents.client.runtime.commands;

import java.util.Objects;

public class CommandResult {
    private final String stdout;
    private final String stderr;
    private final int exitCode;
    /** Server-side error message delivered in the end event (envd reports
     * e.g. spawn failures there); empty when the process exited normally.
     * Preserved like the official SDKs (Python CommandResult.error, Go
     * CommandResult.Error / CommandExitError.ErrorMessage). */
    private final String error;

    public CommandResult(String stdout, String stderr, int exitCode) {
        this(stdout, stderr, exitCode, "");
    }

    public CommandResult(String stdout, String stderr, int exitCode, String error) {
        this.stdout = stdout != null ? stdout : "";
        this.stderr = stderr != null ? stderr : "";
        this.exitCode = exitCode;
        this.error = error != null ? error : "";
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getError() {
        return error;
    }

    public boolean isSuccessful() {
        return exitCode == 0;
    }

    public boolean isFailed() {
        return exitCode != 0;
    }

    public String getOutput() {
        return stdout + (stderr.isEmpty() ? "" : "\n" + stderr);
    }

    @Override
    public String toString() {
        return String.format("CommandResult{exitCode=%d, stdout='%s', stderr='%s', error='%s'}",
            exitCode, stdout, stderr, error);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        CommandResult that = (CommandResult)o;
        return exitCode == that.exitCode &&
            Objects.equals(stdout, that.stdout) &&
            Objects.equals(stderr, that.stderr) &&
            Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stdout, stderr, exitCode, error);
    }
}
