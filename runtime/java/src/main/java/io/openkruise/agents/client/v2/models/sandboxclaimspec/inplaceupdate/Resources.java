package io.openkruise.agents.client.v2.models.sandboxclaimspec.inplaceupdate;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"limits","requests"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Resources implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Limits specifies the target resource limits for each container.
     * Only CPU is supported for now. The container's original limit must already be set;
     * otherwise the value is ignored.
     * The new value must not change the Pod's QoS class; otherwise the claim will be rejected.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("limits")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Limits specifies the target resource limits for each container.\nOnly CPU is supported for now. The container's original limit must already be set;\notherwise the value is ignored.\nThe new value must not change the Pod's QoS class; otherwise the claim will be rejected.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.Map<java.lang.String, io.fabric8.kubernetes.api.model.IntOrString> limits;

    public java.util.Map<java.lang.String, io.fabric8.kubernetes.api.model.IntOrString> getLimits() {
        return limits;
    }

    public void setLimits(java.util.Map<java.lang.String, io.fabric8.kubernetes.api.model.IntOrString> limits) {
        this.limits = limits;
    }

    /**
     * Requests specifies the target resource requests for each container.
     * Only CPU is supported for now. The container's original request must already be set;
     * otherwise the value is ignored.
     * The new value must not change the Pod's QoS class; otherwise the claim will be rejected.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("requests")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Requests specifies the target resource requests for each container.\nOnly CPU is supported for now. The container's original request must already be set;\notherwise the value is ignored.\nThe new value must not change the Pod's QoS class; otherwise the claim will be rejected.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.Map<java.lang.String, io.fabric8.kubernetes.api.model.IntOrString> requests;

    public java.util.Map<java.lang.String, io.fabric8.kubernetes.api.model.IntOrString> getRequests() {
        return requests;
    }

    public void setRequests(java.util.Map<java.lang.String, io.fabric8.kubernetes.api.model.IntOrString> requests) {
        this.requests = requests;
    }
}

