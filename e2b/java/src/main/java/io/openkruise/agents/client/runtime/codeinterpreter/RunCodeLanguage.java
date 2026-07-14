package io.openkruise.agents.client.runtime.codeinterpreter;

/**
 * Supported programming languages for code execution.
 */
public enum RunCodeLanguage {
    PYTHON("python"),
    JAVASCRIPT("javascript"),
    TYPESCRIPT("typescript"),
    R("r"),
    JAVA("java"),
    BASH("bash");
    
    private final String value;
    
    RunCodeLanguage(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
