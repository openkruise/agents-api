package io.openkruise.agents.client.e2b;

import io.openkruise.agents.client.e2b.api.SandboxesApi;
import io.openkruise.agents.client.e2b.api.invoker.ApiClient;
import io.openkruise.agents.client.runtime.RuntimeConfig;
import io.openkruise.agents.client.url.E2BURLBuilder;
import io.openkruise.agents.client.url.URLBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * E2B control plane connection configuration with Builder pattern support.
 */
public class ConnectionConfig {

    public enum Protocol {
        NATIVE("native"),
        PRIVATE("private");

        private final String value;

        Protocol(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private static final String DEFAULT_DOMAIN = "your.domain.com";
    private static final String DEFAULT_SCHEME = "https";
    private static final long DEFAULT_REQUEST_TIMEOUT_MS = 60_000L;
    static final int DEFAULT_SANDBOX_TIMEOUT = 300;

    private String apiKey;
    private String accessToken;
    private String domain;
    private String scheme;
    private Protocol protocol;
    private String apiURL;
    private String sandboxBaseURL;
    private boolean debug;
    private long requestTimeoutMs;
    private int port;
    private int codeInterpreterPort;
    private Map<String, String> headers;
    private ProxyConfig proxyConfig;

    private volatile ApiClient apiClient;
    private volatile SandboxesApi sandboxesApi;
    private final Object lock = new Object();
    
    private URLBuilder urlBuilder;

    private ConnectionConfig() {
        this.domain = DEFAULT_DOMAIN;
        this.scheme = DEFAULT_SCHEME;
        this.protocol = Protocol.NATIVE;
        this.requestTimeoutMs = DEFAULT_REQUEST_TIMEOUT_MS;
        this.port = RuntimeConfig.DEFAULT_RUNTIME_PORT;
        this.codeInterpreterPort = RuntimeConfig.DEFAULT_CODE_INTERPRETER_PORT;
        this.headers = new HashMap<>();
        this.urlBuilder = buildURLBuilder();
    }

    private ConnectionConfig(ConnectionConfig config) {
        this.apiKey = config.apiKey;
        this.accessToken = config.accessToken;
        this.domain = config.domain;
        this.scheme = config.scheme;
        this.protocol = config.protocol;
        this.apiURL = config.apiURL;
        this.sandboxBaseURL = config.sandboxBaseURL;
        this.debug = config.debug;
        this.requestTimeoutMs = config.requestTimeoutMs;
        this.port = config.port;
        this.codeInterpreterPort = config.codeInterpreterPort;
        this.headers = new HashMap<>(config.headers);
        this.proxyConfig = config.proxyConfig;
        this.urlBuilder = buildURLBuilder();
    }
    
    private URLBuilder buildURLBuilder() {
        return new E2BURLBuilder.Builder()
            .scheme(scheme)
            .domain(domain)
            .protocol(protocol)
            .runtimePort(port)
            .codeInterpreterPort(codeInterpreterPort)
            .customApiURL(apiURL)
            .customSandboxBaseURL(sandboxBaseURL)
            .build();
    }

    public static ConnectionConfig create() {
        return new Builder().build();
    }

    /**
     * Convert ConnectionConfig to RuntimeConfig for data plane operations.
     */
    public static RuntimeConfig toRuntimeConfig(ConnectionConfig connectionConfig, String envdAccessToken) {
        if (connectionConfig == null) {
            throw new IllegalArgumentException("connectionConfig cannot be null");
        }
        
        RuntimeConfig.Builder builder = new RuntimeConfig.Builder();
        builder.domain(connectionConfig.getDomain());
        builder.scheme(connectionConfig.getScheme());
        builder.requestTimeoutMs(connectionConfig.getRequestTimeoutMs());

        if (connectionConfig.getApiKey() != null) {
            builder.apiKey(connectionConfig.getApiKey());
        }
        if (connectionConfig.getHeaders() != null) {
            builder.headers(connectionConfig.getHeaders());
        }
        if (envdAccessToken != null && !envdAccessToken.isEmpty()) {
            builder.runtimeToken(envdAccessToken);
        }

        builder.sandboxPort(connectionConfig.getPort());
        builder.codeInterpreterPort(connectionConfig.getCodeInterpreterPort());

        // Build URLBuilder for E2B
        URLBuilder urlBuilder = new E2BURLBuilder.Builder()
            .scheme(connectionConfig.getScheme())
            .domain(connectionConfig.getDomain())
            .protocol(connectionConfig.getProtocol())
            .runtimePort(connectionConfig.getPort())
            .codeInterpreterPort(connectionConfig.getCodeInterpreterPort())
            .customApiURL(connectionConfig.getApiURL())
            .customSandboxBaseURL(connectionConfig.getSandboxBaseURL())
            .build();

        builder.urlBuilder(urlBuilder);
        
        // Pass proxy configuration
        builder.proxyConfig(connectionConfig.getProxyConfig());

        return builder.build();
    }

    /** API base URL, automatically selects NATIVE/PRIVATE format based on protocol. */
    public String getAPIURL() {
        return urlBuilder.buildAPIURL();
    }

    /** Envd URL for a specific sandbox, automatically selects NATIVE/PRIVATE format based on protocol. */
    public String getSandboxURL(String sandboxID) {
        return urlBuilder.buildSandboxURL(sandboxID);
    }

    /** Code Interpreter URL for a specific sandbox, uses codeInterpreterPort (49999). */
    public String getCodeInterpreterURL(String sandboxID) {
        return urlBuilder.buildCodeInterpreterURL(sandboxID);
    }

    /** Shared ApiClient with double-checked locking lazy initialization. */
    public ApiClient getOrCreateApiClient() {
        if (apiClient == null) {
            synchronized (lock) {
                if (apiClient == null) {
                    ApiClient client = new ApiClient();
                    // Apply proxy configuration if enabled
                    if (proxyConfig != null) {
                        client.setHttpClient(proxyConfig.createHttpClient());
                    }
                    client.setBasePath(getAPIURL());
                    client.setConnectTimeout((int)requestTimeoutMs);
                    client.setReadTimeout((int)requestTimeoutMs);
                    if (apiKey != null && !apiKey.isEmpty()) {
                        client.addDefaultHeader("X-API-Key", apiKey);
                    }
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        client.addDefaultHeader(entry.getKey(), entry.getValue());
                    }
                    this.apiClient = client;
                }
            }
        }
        return apiClient;
    }

    SandboxesApi getOrCreateSandboxesApi() {
        if (sandboxesApi == null) {
            synchronized (lock) {
                if (sandboxesApi == null) {
                    sandboxesApi = new SandboxesApi(getOrCreateApiClient());
                }
            }
        }
        return sandboxesApi;
    }

    private String getSchemeOrDefault() {
        return (scheme != null && !scheme.isEmpty()) ? scheme : DEFAULT_SCHEME;
    }

    public String getApiKey() {return apiKey;}

    public String getAccessToken() {return accessToken;}

    public String getDomain() {return domain;}

    public String getScheme() {return scheme;}

    public Protocol getProtocol() {return protocol;}

    public String getApiURL() {return apiURL;}

    public String getSandboxBaseURL() {return sandboxBaseURL;}

    public boolean isDebug() {return debug;}

    public long getRequestTimeoutMs() {return requestTimeoutMs;}

    public int getPort() {return port;}

    public int getCodeInterpreterPort() {return codeInterpreterPort;}

    public Map<String, String> getHeaders() {return headers;}

    public ProxyConfig getProxyConfig() {return proxyConfig;}

    public static class Builder {
        private final ConnectionConfig config = new ConnectionConfig();

        public Builder() {
            // Environment variables as defaults
            String envApiKey = System.getenv("E2B_API_KEY");
            if (envApiKey != null && !envApiKey.isEmpty()) {
                config.apiKey = envApiKey;
            }
            String domain = System.getenv("E2B_DOMAIN");
            if (domain != null && !domain.isEmpty()) {
                config.domain = domain;
            }
        }

        public Builder apiKey(String apiKey) {
            config.apiKey = apiKey;
            return this;
        }

        public Builder accessToken(String accessToken) {
            config.accessToken = accessToken;
            return this;
        }

        public Builder domain(String domain) {
            config.domain = domain;
            return this;
        }

        public Builder scheme(String scheme) {
            config.scheme = scheme;
            return this;
        }

        public Builder protocol(Protocol protocol) {
            config.protocol = protocol;
            return this;
        }

        public Builder apiURL(String apiURL) {
            config.apiURL = apiURL;
            return this;
        }

        public Builder sandboxBaseURL(String sandboxBaseURL) {
            config.sandboxBaseURL = sandboxBaseURL;
            return this;
        }

        public Builder debug(boolean debug) {
            config.debug = debug;
            return this;
        }

        public Builder requestTimeoutMs(long timeoutMs) {
            config.requestTimeoutMs = timeoutMs;
            return this;
        }

        public Builder port(int port) {
            config.port = port;
            return this;
        }

        public Builder codeInterpreterPort(int codeInterpreterPort) {
            config.codeInterpreterPort = codeInterpreterPort;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            config.headers.putAll(headers);
            return this;
        }

        public Builder addHeader(String key, String value) {
            config.headers.put(key, value);
            return this;
        }

        public Builder proxyConfig(ProxyConfig proxyConfig) {
            config.proxyConfig = proxyConfig;
            return this;
        }

        public ConnectionConfig build() {
            config.urlBuilder = config.buildURLBuilder();
            return new ConnectionConfig(config);
        }
    }
}
