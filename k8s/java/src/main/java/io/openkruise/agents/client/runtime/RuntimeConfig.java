package io.openkruise.agents.client.runtime;

import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.openkruise.agents.client.e2b.ProxyConfig;
import io.openkruise.agents.client.url.URLBuilder;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Runtime connection configuration with Builder pattern.
 * Supports both direct runtime connection and E2B mode (via URLBuilder).
 */
public class RuntimeConfig {

    private static final String DEFAULT_DOMAIN = "domain.app";
    private static final String DEFAULT_SCHEME = "http";
    private static final long DEFAULT_REQUEST_TIMEOUT_MS = 60_000L;
    static final String DEFAULT_AUTH_HEADER = "Basic cm9vdDo=";
    public static final int DEFAULT_RUNTIME_PORT = 49983;
    public static final int DEFAULT_CODE_INTERPRETER_PORT = 49999;

    private final String domain;
    private final String scheme;
    private final String runtimeToken;
    private final String runtimeUrl;
    private final String authHeader;
    private final String apiKey;
    private final Map<String, String> headers;
    private final long requestTimeoutMs;
    private final URLBuilder urlBuilder;
    private final int sandboxPort;
    private final int codeInterpreterPort;
    private final ProxyConfig proxyConfig;

    protected RuntimeConfig(Builder builder) {
        this.domain = builder.domain;
        this.scheme = builder.scheme;
        this.runtimeToken = builder.runtimeToken;
        this.runtimeUrl = builder.runtimeUrl;
        this.authHeader = builder.authHeader;
        this.apiKey = builder.apiKey;
        this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
        this.requestTimeoutMs = builder.requestTimeoutMs;
        this.urlBuilder = builder.urlBuilder;
        this.sandboxPort = builder.sandboxPort;
        this.codeInterpreterPort = builder.codeInterpreterPort;
        this.proxyConfig = builder.proxyConfig;
    }

    /**
     * Runtime base URL, uses URLBuilder if available, otherwise returns runtimeUrl or scheme://domain.
     */
    public String getSandboxURL(String sandboxID) {
        if (urlBuilder != null) {
            return urlBuilder.buildSandboxURL(sandboxID);
        }
        if (runtimeUrl != null && !runtimeUrl.isEmpty()) {
            return runtimeUrl;
        }
        // Default: <scheme>://<domain>
        return String.format("%s://%s", getScheme(), domain);
    }

    /**
     * Code Interpreter URL for a specific sandbox, uses codeInterpreterPort (49999).
     */
    public String getCodeInterpreterURL(String sandboxID) {
        if (urlBuilder != null) {
            return urlBuilder.buildCodeInterpreterURL(sandboxID);
        }
        // Fallback: use same base URL as sandbox
        return getSandboxURL(sandboxID);
    }

    /**
     * Builds common authentication headers (Authorization, X-Access-Token, X-API-Key, e2b-sandbox-id, e2b-sandbox-port, etc.), extensible by subclasses.
     */
    public Map<String, String> getSandboxHeaders(String sandboxID) {
        Map<String, String> result = new HashMap<>(5 + headers.size());

        String auth = (authHeader != null && !authHeader.isEmpty()) ? authHeader : DEFAULT_AUTH_HEADER;
        result.put("Authorization", auth);

        if (runtimeToken != null && !runtimeToken.isEmpty()) {
            result.put("X-Access-Token", runtimeToken);
        }

        if (apiKey != null && !apiKey.isEmpty()) {
            result.put("X-API-Key", apiKey);
        }

        if (sandboxID != null && !sandboxID.isEmpty()) {
            result.put("e2b-sandbox-id", sandboxID);
        }

        if (sandboxPort > 0) {
            result.put("e2b-sandbox-port", String.valueOf(sandboxPort));
        }

        result.putAll(headers);
        return result;
    }

    /**
     * Get headers for code interpreter operations.
     * Overrides e2b-sandbox-port with codeInterpreterPort.
     */
    public Map<String, String> getCodeInterpreterHeaders(String sandboxID) {
        Map<String, String> result = new HashMap<>(getSandboxHeaders(sandboxID));
        if (codeInterpreterPort > 0) {
            result.put("e2b-sandbox-port", String.valueOf(codeInterpreterPort));
        }
        return result;
    }

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final MediaType CONNECT_PROTO_MEDIA_TYPE = MediaType.get("application/connect+proto");
    private static final JsonFormat.Printer PROTO_PRINTER = JsonFormat.printer().omittingInsignificantWhitespace();

    private volatile OkHttpClient sharedHttpClient;
    private volatile OkHttpClient sharedStreamingClient;
    private final Object httpClientLock = new Object();
    private volatile boolean shutdown;

    /**
     * Shared OkHttpClient with double-checked locking lazy initialization, reused by all RuntimeClients.
     */
    public OkHttpClient getOrCreateHttpClient() {
        if (sharedHttpClient == null) {
            synchronized (httpClientLock) {
                if (shutdown) {
                    throw new IllegalStateException("RuntimeConfig has been shut down");
                }
                if (sharedHttpClient == null) {
                    OkHttpClient.Builder builder = new OkHttpClient.Builder()
                        .connectTimeout(requestTimeoutMs, TimeUnit.MILLISECONDS)
                        .readTimeout(requestTimeoutMs, TimeUnit.MILLISECONDS)
                        .writeTimeout(requestTimeoutMs, TimeUnit.MILLISECONDS);
                    
                    // Apply proxy configuration if enabled
                    if (proxyConfig != null) {
                        proxyConfig.applyTo(builder);
                    }
                    
                    sharedHttpClient = builder.build();
                }
            }
        }
        return sharedHttpClient;
    }

    /**
     * Shared streaming OkHttpClient (no read timeout), reused by all RuntimeClients.
     */
    public OkHttpClient getOrCreateStreamingHttpClient() {
        if (sharedStreamingClient == null) {
            synchronized (httpClientLock) {
                if (shutdown) {
                    throw new IllegalStateException("RuntimeConfig has been shut down");
                }
                if (sharedStreamingClient == null) {
                    OkHttpClient.Builder builder = new OkHttpClient.Builder()
                        .connectTimeout(requestTimeoutMs, TimeUnit.MILLISECONDS)
                        .readTimeout(0, TimeUnit.MILLISECONDS)
                        .writeTimeout(requestTimeoutMs, TimeUnit.MILLISECONDS);
                    
                    // Apply proxy configuration if enabled
                    if (proxyConfig != null) {
                        proxyConfig.applyTo(builder);
                    }
                    
                    sharedStreamingClient = builder.build();
                }
            }
        }
        return sharedStreamingClient;
    }

    /**
     * Shuts down the shared OkHttpClient thread pools (Dispatcher and ConnectionPool).
     * Should be called when all RuntimeClients using this config are no longer needed.
     */
    public void shutdown() {
        synchronized (httpClientLock) {
            shutdown = true;
            if (sharedHttpClient != null) {
                sharedHttpClient.dispatcher().executorService().shutdown();
                sharedHttpClient.connectionPool().evictAll();
            }
            if (sharedStreamingClient != null) {
                sharedStreamingClient.dispatcher().executorService().shutdown();
                sharedStreamingClient.connectionPool().evictAll();
            }
        }
    }

    /**
     * Builds HTTP/JSON unary call requests (List, SendInput, etc.).
     */
    public Request buildHttpRequest(String serviceName, String methodName,
        MessageOrBuilder message, String sandboxID) {
        try {
            String jsonBody = PROTO_PRINTER.print(message);
            String url = String.format("%s/%s/%s", getSandboxURL(sandboxID), serviceName, methodName);

            Request.Builder reqBuilder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonBody, JSON_MEDIA_TYPE));

            // Set request headers
            Map<String, String> hdrs = getSandboxHeaders(sandboxID);
            for (Map.Entry<String, String> entry : hdrs.entrySet()) {
                reqBuilder.addHeader(entry.getKey(), entry.getValue());
            }

            return reqBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build HTTP request", e);
        }
    }

    /**
     * Builds Connect Protocol streaming requests (Server-Streaming, e.g., Start, Connect).
     * Frame format: 1 byte flags (0x00) + 4 bytes big-endian length + protobuf body.
     */
    public Request buildStreamingRequest(String serviceName, String methodName,
        Message message, String sandboxID) {
        String url = String.format("%s/%s/%s", getSandboxURL(sandboxID), serviceName, methodName);

        // Connect Protocol streaming request: body needs to be wrapped in Enveloped-Message
        // Frame format: 1 byte flags (0x00=data) + 4 bytes big-endian payload length + payload
        byte[] payload = message.toByteArray();
        byte[] enveloped = new byte[5 + payload.length];
        enveloped[0] = 0x00;
        enveloped[1] = (byte)((payload.length >> 24) & 0xFF);
        enveloped[2] = (byte)((payload.length >> 16) & 0xFF);
        enveloped[3] = (byte)((payload.length >> 8) & 0xFF);
        enveloped[4] = (byte)(payload.length & 0xFF);
        System.arraycopy(payload, 0, enveloped, 5, payload.length);

        Request.Builder reqBuilder = new Request.Builder()
            .url(url)
            .post(RequestBody.create(enveloped, CONNECT_PROTO_MEDIA_TYPE))
            .addHeader("Connect-Protocol-Version", "1");

        Map<String, String> hdrs = getSandboxHeaders(sandboxID);
        for (Map.Entry<String, String> entry : hdrs.entrySet()) {
            reqBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        return reqBuilder.build();
    }

    public String getDomain() {
        return domain;
    }

    public String getScheme() {
        return (scheme != null && !scheme.isEmpty()) ? scheme : DEFAULT_SCHEME;
    }

    public String getRuntimeToken() {
        return runtimeToken;
    }

    public String getRuntimeUrl() {
        return runtimeUrl;
    }

    public String getAuthHeader() {
        return authHeader;
    }

    public String getApiKey() {
        return apiKey;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public long getRequestTimeoutMs() {
        return requestTimeoutMs;
    }

    public int getSandboxPort() {
        return sandboxPort;
    }

    public int getCodeInterpreterPort() {
        return codeInterpreterPort;
    }

    public URLBuilder getUrlBuilder() {
        return urlBuilder;
    }

    public ProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    /**
     * Builder pattern, extensible by subclasses.
     */
    public static class Builder {
        protected String domain = DEFAULT_DOMAIN;
        protected String scheme = DEFAULT_SCHEME;
        protected String runtimeToken;
        protected String runtimeUrl;
        protected String authHeader = DEFAULT_AUTH_HEADER;
        protected String apiKey;
        protected Map<String, String> headers = new HashMap<>();
        protected long requestTimeoutMs = DEFAULT_REQUEST_TIMEOUT_MS;
        protected URLBuilder urlBuilder;
        protected int sandboxPort = DEFAULT_RUNTIME_PORT;
        protected int codeInterpreterPort = DEFAULT_CODE_INTERPRETER_PORT;
        protected ProxyConfig proxyConfig;

        public Builder() {}

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder runtimeToken(String runtimeToken) {
            this.runtimeToken = runtimeToken;
            return this;
        }

        /**
         * Sets direct runtime URL; getSandboxURL() returns this URL directly when set.
         */
        public Builder runtimeUrl(String runtimeUrl) {
            this.runtimeUrl = runtimeUrl;
            return this;
        }

        public Builder authHeader(String authHeader) {
            this.authHeader = authHeader;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public Builder addHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder requestTimeoutMs(long requestTimeoutMs) {
            this.requestTimeoutMs = requestTimeoutMs;
            return this;
        }

        public Builder urlBuilder(URLBuilder urlBuilder) {
            this.urlBuilder = urlBuilder;
            return this;
        }

        public Builder sandboxPort(int sandboxPort) {
            this.sandboxPort = sandboxPort;
            return this;
        }

        public Builder codeInterpreterPort(int codeInterpreterPort) {
            this.codeInterpreterPort = codeInterpreterPort;
            return this;
        }

        public Builder proxyConfig(ProxyConfig proxyConfig) {
            this.proxyConfig = proxyConfig;
            return this;
        }

        public RuntimeConfig build() {
            return new RuntimeConfig(this);
        }
    }
}
