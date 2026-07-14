package io.openkruise.agents.client.v2.models.sandboxsetspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"maxUnavailable"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class ScaleStrategy implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * The maximum number of sandboxes that can be unavailable for scaled sandboxes.
     * This field can control the changes rate of replicas for SandboxSet so as to minimize the impact for users' service.
     * The scale will fail if the number of unavailable sandboxes were greater than this MaxUnavailable at scaling up.
     * MaxUnavailable works only when scaling up.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("maxUnavailable")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("The maximum number of sandboxes that can be unavailable for scaled sandboxes.\nThis field can control the changes rate of replicas for SandboxSet so as to minimize the impact for users' service.\nThe scale will fail if the number of unavailable sandboxes were greater than this MaxUnavailable at scaling up.\nMaxUnavailable works only when scaling up.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.fabric8.kubernetes.api.model.IntOrString maxUnavailable;

    public io.fabric8.kubernetes.api.model.IntOrString getMaxUnavailable() {
        return maxUnavailable;
    }

    public void setMaxUnavailable(io.fabric8.kubernetes.api.model.IntOrString maxUnavailable) {
        this.maxUnavailable = maxUnavailable;
    }
}

