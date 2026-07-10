package io.openkruise.agents.client.url;

/**
 * URL builder interface for constructing sandbox and API URLs.
 * Provides a unified contract for different URL construction strategies.
 */
public interface URLBuilder {

    /**
     * Build API base URL for control plane operations.
     *
     * @return API base URL
     */
    String buildAPIURL();

    /**
     * Build sandbox URL for envd operations (port 49983).
     *
     * @param sandboxID sandbox identifier
     * @return sandbox URL for commands and files
     */
    String buildSandboxURL(String sandboxID);

    /**
     * Build code interpreter URL for code execution (port 49999).
     *
     * @param sandboxID sandbox identifier
     * @return code interpreter URL
     */
    String buildCodeInterpreterURL(String sandboxID);

    /**
     * Build URL with custom port.
     *
     * @param sandboxID sandbox identifier
     * @param port custom port number
     * @return URL with specified port
     */
    String buildURLWithPort(String sandboxID, int port);
}
