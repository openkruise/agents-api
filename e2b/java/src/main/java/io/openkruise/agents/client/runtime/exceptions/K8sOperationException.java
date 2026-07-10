package io.openkruise.agents.client.runtime.exceptions;

/**
 * Exception thrown when Kubernetes API operations fail (e.g., Sandbox CR not found, API call errors).
 */
public class K8sOperationException extends RuntimeException {

    public K8sOperationException(String message) {
        super(message);
    }

    public K8sOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
