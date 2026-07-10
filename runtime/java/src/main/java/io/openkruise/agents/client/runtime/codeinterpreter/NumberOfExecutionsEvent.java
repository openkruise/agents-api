package io.openkruise.agents.client.runtime.codeinterpreter;

/**
 * Execution count update event.
 */
public class NumberOfExecutionsEvent extends ExecutionEvent {
    private final int executionCount;

    public NumberOfExecutionsEvent(int executionCount) {
        super("number_of_executions");
        this.executionCount = executionCount;
    }

    public int getExecutionCount() { return executionCount; }

    @Override
    public String toString() {
        return "NumberOfExecutionsEvent{executionCount=" + executionCount + "}";
    }
}
