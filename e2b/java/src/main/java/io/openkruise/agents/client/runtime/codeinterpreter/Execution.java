package io.openkruise.agents.client.runtime.codeinterpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents the result of a code execution.
 */
public class Execution {
    private List<Result> results;
    private Logs logs;
    private ExecutionError error;
    /** Execution count, null if no number_of_executions event was received. */
    private Integer executionCount;
    
    public Execution() {
        this.results = new ArrayList<>();
        this.logs = new Logs();
    }
    
    public List<Result> getResults() {
        return results;
    }
    
    public void setResults(List<Result> results) {
        this.results = results;
    }
    
    public Logs getLogs() {
        return logs;
    }
    
    public void setLogs(Logs logs) {
        this.logs = logs;
    }
    
    public ExecutionError getError() {
        return error;
    }
    
    public void setError(ExecutionError error) {
        this.error = error;
    }
    
    public Integer getExecutionCount() {
        return executionCount;
    }
    
    public void setExecutionCount(Integer executionCount) {
        this.executionCount = executionCount;
    }
    
    /**
     * Returns the text representation of the main result.
     * @return the text or null if not found
     */
    public String getText() {
        for (Result result : results) {
            if (result.isMainResult() && result.getText() != null) {
                return result.getText();
            }
        }
        return null;
    }
    
    /**
     * Returns the text representation of the main result as Optional.
     * @return Optional containing the text if found, empty otherwise
     */
    public Optional<String> findText() {
        return results.stream()
            .filter(Result::isMainResult)
            .map(Result::getText)
            .filter(Objects::nonNull)
            .findFirst();
    }
    
    @Override
    public String toString() {
        return String.format("Execution(results: %d, logs: %s, error: %s)", 
            results.size(), logs, error);
    }
}
