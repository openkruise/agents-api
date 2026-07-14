package io.openkruise.agents.client.v2.models.sandboxupdateopsspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"maxUnavailable"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class UpdateStrategy implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * MaxUnavailable is the maximum number of sandboxes that can be upgrading at the same time.
     * Value can be an absolute number (e.g., 5) or a percentage of total sandboxes (e.g., 10%).
     */
    @com.fasterxml.jackson.annotation.JsonProperty("maxUnavailable")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("MaxUnavailable is the maximum number of sandboxes that can be upgrading at the same time.\nValue can be an absolute number (e.g., 5) or a percentage of total sandboxes (e.g., 10%).")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.fabric8.kubernetes.api.model.IntOrString maxUnavailable;

    public io.fabric8.kubernetes.api.model.IntOrString getMaxUnavailable() {
        return maxUnavailable;
    }

    public void setMaxUnavailable(io.fabric8.kubernetes.api.model.IntOrString maxUnavailable) {
        this.maxUnavailable = maxUnavailable;
    }
}

