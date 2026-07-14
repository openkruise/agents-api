package io.openkruise.agents.client.e2b;

import io.openkruise.agents.client.e2b.api.invoker.ApiException;

/**
 * Thrown when the specified sandbox does not exist (HTTP 404).
 */
public class SandboxNotFoundException extends ApiException {

    private final String sandboxID;

    public SandboxNotFoundException(String sandboxID, ApiException cause) {
        super("sandbox " + sandboxID + " not found", cause,
            404, cause.getResponseHeaders(), cause.getResponseBody());
        this.sandboxID = sandboxID;
    }

    /**
     * Returns the ID of the sandbox that was not found.
     */
    public String getSandboxID() {
        return sandboxID;
    }
}
