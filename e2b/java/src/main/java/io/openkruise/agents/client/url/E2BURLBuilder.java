package io.openkruise.agents.client.url;

import io.openkruise.agents.client.e2b.ConnectionConfig;
import io.openkruise.agents.client.runtime.RuntimeConfig;

/**
 * URL builder for E2B platform supporting NATIVE and PRIVATE protocols.
 * 
 * NATIVE mode:
 * - API URL: <scheme>://api.<domain>
 * - Sandbox URL: <scheme>://<port>-<sandboxID>.<domain>
 * - Code URL: <scheme>://<port>-<sandboxID>.<domain>
 * 
 * PRIVATE mode:
 * - API URL: <scheme>://<domain>/kruise/api
 * - Sandbox URL: <scheme>://<domain>/kruise/<sandboxID>/<port>
 * - Code URL: <scheme>://<domain>/kruise/<sandboxID>/<port>
 */
public class E2BURLBuilder implements URLBuilder {

    private final String scheme;
    private final String domain;
    private final ConnectionConfig.Protocol protocol;
    private final int runtimePort;
    private final int codeInterpreterPort;
    private final String customApiURL;
    private final String customSandboxBaseURL;

    public E2BURLBuilder(String scheme, String domain, ConnectionConfig.Protocol protocol,
                         int runtimePort, int codeInterpreterPort,
                         String customApiURL, String customSandboxBaseURL) {
        this.scheme = (scheme != null && !scheme.isEmpty()) ? scheme : "https";
        this.domain = domain;
        this.protocol = protocol;
        this.runtimePort = runtimePort;
        this.codeInterpreterPort = codeInterpreterPort;
        this.customApiURL = customApiURL;
        this.customSandboxBaseURL = customSandboxBaseURL;
    }

    @Override
    public String buildAPIURL() {
        if (customApiURL != null && !customApiURL.isEmpty()) {
            return customApiURL;
        }
        
        if (protocol == ConnectionConfig.Protocol.PRIVATE) {
            return String.format("%s://%s/kruise/api", scheme, domain);
        }
        return String.format("%s://api.%s", scheme, domain);
    }

    @Override
    public String buildSandboxURL(String sandboxID) {
        return buildURLWithPort(sandboxID, runtimePort);
    }

    @Override
    public String buildCodeInterpreterURL(String sandboxID) {
        return buildURLWithPort(sandboxID, codeInterpreterPort);
    }

    @Override
    public String buildURLWithPort(String sandboxID, int port) {
        // Priority 1: customSandboxBaseURL (explicit override) — return directly
        if (customSandboxBaseURL != null && !customSandboxBaseURL.isEmpty()) {
            return customSandboxBaseURL.replaceAll("/+$", "");
        }

        // Priority 2: protocol + domain assembly
        return (protocol == ConnectionConfig.Protocol.PRIVATE)
            ? String.format("%s://%s/kruise/%s/%d", scheme, domain, sandboxID, port)
            : String.format("%s://%d-%s.%s", scheme, port, sandboxID, domain);
    }

    /**
     * Builder for E2BURLBuilder.
     */
    public static class Builder {
        private String scheme = "https";
        private String domain;
        private ConnectionConfig.Protocol protocol = ConnectionConfig.Protocol.NATIVE;
        private int runtimePort = RuntimeConfig.DEFAULT_RUNTIME_PORT;
        private int codeInterpreterPort = RuntimeConfig.DEFAULT_CODE_INTERPRETER_PORT;
        private String customApiURL;
        private String customSandboxBaseURL;

        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder protocol(ConnectionConfig.Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder runtimePort(int runtimePort) {
            this.runtimePort = runtimePort;
            return this;
        }

        public Builder codeInterpreterPort(int codeInterpreterPort) {
            this.codeInterpreterPort = codeInterpreterPort;
            return this;
        }

        public Builder customApiURL(String customApiURL) {
            this.customApiURL = customApiURL;
            return this;
        }

        public Builder customSandboxBaseURL(String customSandboxBaseURL) {
            this.customSandboxBaseURL = customSandboxBaseURL;
            return this;
        }

        public E2BURLBuilder build() {
            return new E2BURLBuilder(scheme, domain, protocol, runtimePort, 
                codeInterpreterPort, customApiURL, customSandboxBaseURL);
        }
    }
}
