package io.openkruise.agents.client.v2.models.sandboxspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"type"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class UpgradePolicy implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Type specifies the upgrade policy type.
     * When empty (default), upgrading is disabled.
     * Supported values: Recreate.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("type")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Type specifies the upgrade policy type.\nWhen empty (default), upgrading is disabled.\nSupported values: Recreate.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

