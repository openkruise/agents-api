package io.openkruise.agents.client.runtime.codeinterpreter;

/**
 * Base class for output events (stdout/stderr).
 */
public abstract class OutputEvent extends ExecutionEvent {
    private final String text;
    private final String timestamp;

    protected OutputEvent(String type, String text, String timestamp) {
        super(type);
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getText() { return text; }
    public String getTimestamp() { return timestamp; }
}
