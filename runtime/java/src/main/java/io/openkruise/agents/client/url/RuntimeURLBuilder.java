package io.openkruise.agents.client.url;

/**
 * URL builder for Runtime direct connection mode.
 * 
 * Runtime mode uses a single base URL for all operations.
 * Port differentiation (49983 for commands/files, 49999 for code) is handled via
 * the "e2b-sandbox-port" header in RuntimeConfig, not in the URL itself.
 */
public class RuntimeURLBuilder implements URLBuilder {

    private final String scheme;
    private final String domain;
    private final String runtimeUrl;

    public RuntimeURLBuilder(String scheme, String domain, String runtimeUrl) {
        this.scheme = (scheme != null && !scheme.isEmpty()) ? scheme : "http";
        this.domain = domain;
        this.runtimeUrl = runtimeUrl;
    }

    @Override
    public String buildAPIURL() {
        // Runtime mode doesn't have a separate API URL
        // Returns the base runtime URL
        return getBaseURL();
    }

    @Override
    public String buildSandboxURL(String sandboxID) {
        // For runtime mode, the URL is the same, port is specified in header
        return getBaseURL();
    }

    @Override
    public String buildCodeInterpreterURL(String sandboxID) {
        // For runtime mode, the URL is the same, port is specified in header
        return getBaseURL();
    }

    @Override
    public String buildURLWithPort(String sandboxID, int port) {
        // For runtime mode, the URL is the same, port is specified in header
        return getBaseURL();
    }

    /**
     * Get the base URL for all operations.
     * Port differentiation is handled via headers.
     */
    private String getBaseURL() {
        if (runtimeUrl != null && !runtimeUrl.isEmpty()) {
            return runtimeUrl;
        }
        return String.format("%s://%s", scheme, domain);
    }

    /**
     * Builder for RuntimeURLBuilder.
     */
    public static class Builder {
        private String scheme = "http";
        private String domain;
        private String runtimeUrl;

        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder runtimeUrl(String runtimeUrl) {
            this.runtimeUrl = runtimeUrl;
            return this;
        }

        public RuntimeURLBuilder build() {
            return new RuntimeURLBuilder(scheme, domain, runtimeUrl);
        }
    }
}
