package io.openkruise.agents.client.runtime.commands;

import java.util.Objects;

public class CommandResult {
    private final String stdout;
    private final String stderr;
    private final int exitCode;

    public CommandResult(String stdout, String stderr, int exitCode) {
        this.stdout = stdout != null ? stdout : "";
        this.stderr = stderr != null ? stderr : "";
        this.exitCode = exitCode;
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
        return String.format("CommandResult{exitCode=%d, stdout='%s', stderr='%s'}",
            exitCode, stdout, stderr);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        CommandResult that = (CommandResult)o;
        return exitCode == that.exitCode &&
            Objects.equals(stdout, that.stdout) &&
            Objects.equals(stderr, that.stderr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stdout, stderr, exitCode);
    }
}
