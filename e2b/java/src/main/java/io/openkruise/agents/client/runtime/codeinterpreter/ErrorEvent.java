package io.openkruise.agents.client.runtime.codeinterpreter;

/**
 * Execution error event.
 */
public class ErrorEvent extends ExecutionEvent {
    private final ExecutionError error;

    public ErrorEvent(ExecutionError error) {
        super("error");
        this.error = error;
    }

    public ExecutionError getError() { return error; }

    @Override
    public String toString() {
        return "ErrorEvent{error=" + error + "}";
    }
}
