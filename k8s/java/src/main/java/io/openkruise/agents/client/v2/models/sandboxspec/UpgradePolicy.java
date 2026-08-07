package io.openkruise.agents.client.v2.models.sandboxspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"type"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class UpgradePolicy implements io.fabric8.kubernetes.api.model.KubernetesResource {

    public enum Type {

        @com.fasterxml.jackson.annotation.JsonProperty("Recreate")
        RECREATE("Recreate"), @com.fasterxml.jackson.annotation.JsonProperty("CheckpointRestore")
        CHECKPOINTRESTORE("CheckpointRestore");

        java.lang.String value;

        Type(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * Type specifies the upgrade policy type.
     * When empty (default), upgrading is disabled.
     * Supported values: Recreate, CheckpointRestore.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("type")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Type specifies the upgrade policy type.\nWhen empty (default), upgrading is disabled.\nSupported values: Recreate, CheckpointRestore.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}

