package io.openkruise.agents.client.runtime.codeinterpreter;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Request object for code execution.
 * Encapsulates all parameters needed for the /execute endpoint.
 */
public class RunCodeRequest {
    private final String code;
    private final String language;
    private final String cwd;
    private final Map<String, String> envVars;
    private final Long timeoutMs;
    private final String contextId;

    public RunCodeRequest(String code, String language) {
        this(code, language, null, null, null);
    }

    public RunCodeRequest(String code, String language, RunCodeOptions options) {
        this(code, language,
            options != null ? options.getCwd() : null,
            options != null ? options.getEnvVars() : null,
            options != null ? options.getTimeoutMs() : null,
            options != null ? options.getContextId() : null);
    }

    public RunCodeRequest(String code, String language, String cwd, Map<String, String> envVars) {
        this(code, language, cwd, envVars, null, null);
    }

    public RunCodeRequest(String code, String language, String cwd, Map<String, String> envVars, Long timeoutMs) {
        this(code, language, cwd, envVars, timeoutMs, null);
    }

    public RunCodeRequest(String code, String language, String cwd, Map<String, String> envVars, Long timeoutMs, String contextId) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code cannot be null or empty");
        }
        if (language == null || language.trim().isEmpty()) {
            throw new IllegalArgumentException("Language cannot be null or empty");
        }
        this.code = code;
        this.language = language;
        this.cwd = cwd;
        this.envVars = envVars != null ? validateEnvVars(envVars) : new HashMap<>();
        this.timeoutMs = timeoutMs;
        this.contextId = contextId;
    }

    private Map<String, String> validateEnvVars(Map<String, String> envVars) {
        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            if (entry.getKey() == null || entry.getKey().trim().isEmpty()) {
                throw new IllegalArgumentException("Environment variable key cannot be null or empty");
            }
            if (entry.getValue() == null) {
                throw new IllegalArgumentException("Environment variable value cannot be null for key: " + entry.getKey());
            }
        }
        return new HashMap<>(envVars);
    }

    public String getCode() { return code; }
    public String getLanguage() { return language; }
    public String getCwd() { return cwd; }
    public Map<String, String> getEnvVars() { return envVars; }
    public Long getTimeoutMs() { return timeoutMs; }
    public String getContextId() { return contextId; }

    /**
     * Serialize to JSON string for the /execute endpoint using Gson.
     */
    public String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("code", code);
        
        // Only one of context_id or language can be provided
        if (contextId != null) {
            json.addProperty("context_id", contextId);
        } else {
            json.addProperty("language", language);
        }
        
        if (cwd != null) {
            json.addProperty("cwd", cwd);
        }
        if (!envVars.isEmpty()) {
            JsonObject envs = new JsonObject();
            for (Map.Entry<String, String> entry : envVars.entrySet()) {
                envs.addProperty(entry.getKey(), entry.getValue());
            }
            json.add("env_vars", envs);
        }
        if (timeoutMs != null) {
            json.addProperty("timeout", timeoutMs / 1000.0);
        }
        return json.toString();
    }
}
