package io.openkruise.agents.client.e2b;

import io.openkruise.agents.client.runtime.codeinterpreter.CodeInterpreter;
import io.openkruise.agents.client.runtime.RuntimeClient;
import io.openkruise.agents.client.runtime.RuntimeConfig;
import io.openkruise.agents.client.runtime.commands.Commands;
import io.openkruise.agents.client.runtime.filesystem.Filesystem;

/**
 * Represents a created/connected E2B sandbox instance (data plane).
 * Supports try-with-resources for automatic resource cleanup (HTTP connection + thread pools).
 * Does NOT terminate the sandbox on close — use {@code sandboxApi.kill(sandboxID)} to terminate.
 */
public class Sandbox implements AutoCloseable {

    /** Command execution within the sandbox */
    public final Commands commands;
    /** File operations within the sandbox */
    public final Filesystem files;
    /** Code interpreter for executing code in the sandbox */
    public final CodeInterpreter codeInterpreter;

    private final String sandboxID;
    private final ConnectionConfig config;
    private final RuntimeClient runtimeClient;

    Sandbox(String sandboxID, String envdAccessToken, ConnectionConfig config) {
        this.sandboxID = sandboxID;
        this.config = config;

        RuntimeConfig runtimeConfig = ConnectionConfig.toRuntimeConfig(config, envdAccessToken);
        this.runtimeClient = RuntimeClient.create(sandboxID, runtimeConfig);
        this.commands = runtimeClient.commands;
        this.files = runtimeClient.files;
        this.codeInterpreter = runtimeClient.codeInterpreter;
    }

    public String getSandboxID() {
        return sandboxID;
    }

    public ConnectionConfig getConfig() {
        return config;
    }

    public String getSandboxURL() {
        return config.getSandboxURL(sandboxID);
    }

    public RuntimeClient getRuntimeClient() {
        return runtimeClient;
    }

    /** Closes the HTTP connection and releases thread pools. Does NOT terminate the sandbox. */
    @Override
    public void close() {
        if (runtimeClient != null) {
            runtimeClient.close();
        }
    }

    @Override
    public String toString() {
        return "Sandbox{sandboxID='" + sandboxID + "'}";
    }
}
