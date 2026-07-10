package io.openkruise.agents.client.runtime.codeinterpreter;

/**
 * End of execution stream event.
 */
public class EndOfExecutionEvent extends ExecutionEvent {
    public EndOfExecutionEvent() {
        super("end_of_execution");
    }

    @Override
    public String toString() {
        return "EndOfExecutionEvent{}";
    }
}
