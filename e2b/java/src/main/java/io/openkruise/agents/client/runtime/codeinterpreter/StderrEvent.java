package io.openkruise.agents.client.runtime.codeinterpreter;

/**
 * Stderr output event.
 */
public class StderrEvent extends OutputEvent {
    public StderrEvent(String text, String timestamp) {
        super("stderr", text, timestamp);
    }

    @Override
    public String toString() {
        return "StderrEvent{text='" + getText() + "', timestamp='" + getTimestamp() + "'}";
    }
}
