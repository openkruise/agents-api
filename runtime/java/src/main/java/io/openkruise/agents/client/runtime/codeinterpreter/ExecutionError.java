package io.openkruise.agents.client.runtime.codeinterpreter;

/**
 * Represents an error that occurred during the execution of code.
 */
public class ExecutionError {
    private final String name;
    private final String value;
    private final String traceback;
    
    public ExecutionError(String name, String value, String traceback) {
        this.name = name;
        this.value = value;
        this.traceback = traceback;
    }
    
    public String getName() {
        return name;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getTraceback() {
        return traceback;
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s\n%s", name, value, traceback);
    }
}
