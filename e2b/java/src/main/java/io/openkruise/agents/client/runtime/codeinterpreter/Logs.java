package io.openkruise.agents.client.runtime.codeinterpreter;

import java.util.ArrayList;
import java.util.List;

/**
 * Data printed to stdout and stderr during execution.
 * <p>
 * <strong>NOT thread-safe.</strong> The caller must ensure that events are applied
 * from a single thread only (e.g., the streaming reader thread in CodeInterpreter).
 */
public class Logs {
    private List<String> stdout;
    private List<String> stderr;
    
    public Logs() {
        this.stdout = new ArrayList<>();
        this.stderr = new ArrayList<>();
    }
    
    public List<String> getStdout() {
        return stdout;
    }
    
    public void setStdout(List<String> stdout) {
        this.stdout = stdout != null ? stdout : new ArrayList<>();
    }
    
    public List<String> getStderr() {
        return stderr;
    }
    
    public void setStderr(List<String> stderr) {
        this.stderr = stderr != null ? stderr : new ArrayList<>();
    }
    
    @Override
    public String toString() {
        return String.format("Logs(stdout: %s, stderr: %s)", stdout, stderr);
    }
}
