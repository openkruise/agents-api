package io.openkruise.agents.client.runtime.codeinterpreter;

/**
 * Stdout output event.
 */
public class StdoutEvent extends OutputEvent {
    public StdoutEvent(String text, String timestamp) {
        super("stdout", text, timestamp);
    }

    @Override
    public String toString() {
        return "StdoutEvent{text='" + getText() + "', timestamp='" + getTimestamp() + "'}";
    }
}
