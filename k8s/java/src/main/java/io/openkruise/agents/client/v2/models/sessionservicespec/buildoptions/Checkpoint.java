package io.openkruise.agents.client.v2.models.sessionservicespec.buildoptions;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"enable"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Checkpoint implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Enable turns artifact capture on for the Build.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("enable")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Enable turns artifact capture on for the Build.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Boolean enable;

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }
}

