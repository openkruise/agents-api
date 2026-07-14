package io.openkruise.agents.client.v2.models.sandboxsetspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"maxUnavailable"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class UpdateStrategy implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * MaxUnavailable is the maximum number or percentage of pods that can be unavailable during the update.
     * MaxUnavailable can be an absolute number (ex: 5) or a percentage of desired pods (ex: 10%).
     * Absolute number is calculated from percentage by rounding down.
     * Default is 20%.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("maxUnavailable")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("MaxUnavailable is the maximum number or percentage of pods that can be unavailable during the update.\nMaxUnavailable can be an absolute number (ex: 5) or a percentage of desired pods (ex: 10%).\nAbsolute number is calculated from percentage by rounding down.\nDefault is 20%.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.fabric8.kubernetes.api.model.IntOrString maxUnavailable;

    public io.fabric8.kubernetes.api.model.IntOrString getMaxUnavailable() {
        return maxUnavailable;
    }

    public void setMaxUnavailable(io.fabric8.kubernetes.api.model.IntOrString maxUnavailable) {
        this.maxUnavailable = maxUnavailable;
    }
}

