package io.openkruise.agents.client.runtime.codeinterpreter;

/**
 * Base class for NDJSON stream events from the /execute endpoint.
 * Each line in the response is a JSON object with a "type" field.
 */
public abstract class ExecutionEvent {
    private final String type;

    protected ExecutionEvent(String type) {
        this.type = type;
    }

    public String getType() { return type; }

    @Override
    public String toString() {
        return "ExecutionEvent{type='" + type + "'}";
    }
}
