package io.openkruise.agents.client.runtime;

import io.openkruise.agents.client.runtime.RuntimeConfig.Builder;
import io.openkruise.agents.client.runtime.codeinterpreter.CodeInterpreter;
import io.openkruise.agents.client.runtime.commands.Commands;
import io.openkruise.agents.client.runtime.exceptions.K8sOperationException;
import io.openkruise.agents.client.runtime.filesystem.Filesystem;
import okhttp3.OkHttpClient;

import java.util.Objects;

/**
 * Unified entry point for the Runtime layer, providing command execution and file operations based on OkHttp + Connect
 * Protocol.
 */
public class RuntimeClient implements AutoCloseable {
    public final Commands commands;
    public final Filesystem files;
    public final CodeInterpreter codeInterpreter;

    private final String sandboxID;
    private final RuntimeConfig config;
    private final String runtimeURL;

    private RuntimeClient(String sandboxID, RuntimeConfig config,
        OkHttpClient httpClient, OkHttpClient streamingClient) {
        this.sandboxID = Objects.requireNonNull(sandboxID, "sandboxID cannot be null");
        this.config = Objects.requireNonNull(config, "config cannot be null");
        this.runtimeURL = config.getSandboxURL(sandboxID);

        this.commands = new Commands(sandboxID, config, httpClient, streamingClient);
        this.files = new Filesystem(sandboxID, config, httpClient, streamingClient);
        this.codeInterpreter = new CodeInterpreter(sandboxID, config);
    }

    public static RuntimeClient create(String sandboxID, RuntimeConfig config) {
        OkHttpClient httpClient = config.getOrCreateHttpClient();
        OkHttpClient streamingClient = config.getOrCreateStreamingHttpClient();
        return new RuntimeClient(sandboxID, config, httpClient, streamingClient);
    }

    public String getSandboxID() {
        return sandboxID;
    }

    public String getRuntimeURL() {
        return runtimeURL;
    }

    public RuntimeConfig getConfig() {
        return config;
    }

    @Override
    public void close() {
        files.closeAllWatchHandles();
        config.shutdown();
    }

    @Override
    public String toString() {
        return "RuntimeClient{sandboxID='" + sandboxID + "', runtimeURL='" + runtimeURL + "'}";
    }
}
