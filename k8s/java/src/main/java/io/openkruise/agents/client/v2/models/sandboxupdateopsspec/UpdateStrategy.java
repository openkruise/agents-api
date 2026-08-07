package io.openkruise.agents.client.v2.models.sandboxupdateopsspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"maxUnavailable","type"})
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
     * Type specifies the update strategy type.
     * When empty, defaults to Recreate.
     * Supported values: Recreate, CheckpointRestore.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("type")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Type specifies the update strategy type.\nWhen empty, defaults to Recreate.\nSupported values: Recreate, CheckpointRestore.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}

