package io.openkruise.agents.client.runtime;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.openkruise.agents.client.runtime.exceptions.K8sOperationException;
import io.openkruise.agents.client.v2.models.Sandbox;

import java.util.Map;

/**
 * K8sHelper provides Kubernetes interaction capabilities for querying Sandbox CR to obtain runtimeToken.
 * <p>
 * Implemented using fabric8 kubernetes-client and v2 CRD model for type-safe CR queries.
 * <p>
 * kubeconfig resolution order (handled automatically by fabric8):
 * KUBECONFIG environment variable → ~/.kube/config → in-cluster config
 */
class K8sHelper {

    /** Sandbox CR annotation key storing runtime access token */
    static final String ANNOTATION_RUNTIME_ACCESS_TOKEN = "agents.kruise.io/runtime-access-token";

    /**
     * Extracts runtimeToken from Sandbox CR annotations.
     *
     * @param namespace   K8s namespace where the sandbox resides
     * @param sandboxName sandbox resource name
     * @return runtimeToken value, or null if not present
     * @throws K8sOperationException if K8s query fails or Sandbox CR not found
     */
    static String getRuntimeToken(String namespace, String sandboxName) throws K8sOperationException {
        try (KubernetesClient client = new KubernetesClientBuilder().build()) {
            Sandbox sandbox = client.resources(Sandbox.class)
                .inNamespace(namespace)
                .withName(sandboxName)
                .get();

            if (sandbox == null) {
                throw new K8sOperationException(
                    String.format("Sandbox CR %s/%s not found", namespace, sandboxName));
            }

            Map<String, String> annotations = sandbox.getMetadata().getAnnotations();
            if (annotations != null) {
                return annotations.get(ANNOTATION_RUNTIME_ACCESS_TOKEN);
            }

            return null;
        } catch (K8sOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new K8sOperationException(
                String.format("Failed to query Sandbox CR %s/%s", namespace, sandboxName), e);
        }
    }
}
