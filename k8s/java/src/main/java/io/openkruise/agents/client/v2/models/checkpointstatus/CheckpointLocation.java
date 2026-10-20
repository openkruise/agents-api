package io.openkruise.agents.client.v2.models.checkpointstatus;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"remote","type"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class CheckpointLocation implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Remote locates checkpoint artifacts in remote storage.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("remote")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Remote locates checkpoint artifacts in remote storage.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.checkpointstatus.checkpointlocation.Remote remote;

    public io.openkruise.agents.client.v2.models.checkpointstatus.checkpointlocation.Remote getRemote() {
        return remote;
    }

    public void setRemote(io.openkruise.agents.client.v2.models.checkpointstatus.checkpointlocation.Remote remote) {
        this.remote = remote;
    }

    public enum Type {

        @com.fasterxml.jackson.annotation.JsonProperty("Remote")
        REMOTE("Remote");

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
     * Type identifies the checkpoint storage backend.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("type")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Type identifies the checkpoint storage backend.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}

