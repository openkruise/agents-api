package io.openkruise.agents.client.runtime.codeinterpreter;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Options for code execution.
 */
public class RunCodeOptions {
    private String cwd;
    private Map<String, String> envVars;
    private Long timeoutMs;
    private String contextId;
    private Consumer<StdoutEvent> onStdout;
    private Consumer<StderrEvent> onStderr;
    private Consumer<Result> onResult;
    private Consumer<ExecutionError> onError;
    
    public RunCodeOptions() {
    }
    
    public String getCwd() {
        return cwd;
    }
    
    public RunCodeOptions setCwd(String cwd) {
        this.cwd = cwd;
        return this;
    }
    
    public Map<String, String> getEnvVars() {
        return envVars;
    }
    
    public RunCodeOptions setEnvVars(Map<String, String> envVars) {
        this.envVars = envVars;
        return this;
    }
    
    public Long getTimeoutMs() {
        return timeoutMs;
    }
    
    public RunCodeOptions setTimeoutMs(Long timeoutMs) {
        this.timeoutMs = timeoutMs;
        return this;
    }

    public String getContextId() {
        return contextId;
    }

    public RunCodeOptions setContextId(String contextId) {
        this.contextId = contextId;
        return this;
    }

    public Consumer<StdoutEvent> getOnStdout() {
        return onStdout;
    }

    public RunCodeOptions setOnStdout(Consumer<StdoutEvent> onStdout) {
        this.onStdout = onStdout;
        return this;
    }

    public Consumer<StderrEvent> getOnStderr() {
        return onStderr;
    }

    public RunCodeOptions setOnStderr(Consumer<StderrEvent> onStderr) {
        this.onStderr = onStderr;
        return this;
    }

    public Consumer<Result> getOnResult() {
        return onResult;
    }

    public RunCodeOptions setOnResult(Consumer<Result> onResult) {
        this.onResult = onResult;
        return this;
    }

    public Consumer<ExecutionError> getOnError() {
        return onError;
    }

    public RunCodeOptions setOnError(Consumer<ExecutionError> onError) {
        this.onError = onError;
        return this;
    }

    /**
     * Returns true if any callback is set.
     */
    public boolean hasCallbacks() {
        return onStdout != null || onStderr != null || onResult != null || onError != null;
    }
    
    @Override
    public String toString() {
        return String.format("RunCodeOptions{cwd='%s', envVars=%s, timeoutMs=%s, contextId='%s'}", 
            cwd, envVars, timeoutMs, contextId);
    }
}
