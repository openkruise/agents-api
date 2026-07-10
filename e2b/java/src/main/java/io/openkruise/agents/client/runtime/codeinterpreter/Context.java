package io.openkruise.agents.client.runtime.codeinterpreter;

/**
 * Represents a context for code execution.
 * A context maintains its own working directory and language environment.
 */
public class Context {
    private final String id;
    private final String language;
    private final String cwd;

    public Context(String id, String language, String cwd) {
        this.id = id;
        this.language = language;
        this.cwd = cwd;
    }

    public String getId() {
        return id;
    }

    public String getLanguage() {
        return language;
    }

    public String getCwd() {
        return cwd;
    }

    @Override
    public String toString() {
        return String.format("Context{id='%s', language='%s', cwd='%s'}", id, language, cwd);
    }
}
