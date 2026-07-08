package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"containerName","image","podName","registryAuth","squashLayer","timeoutSeconds","ttl"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class CommitSpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

    @com.fasterxml.jackson.annotation.JsonProperty("containerName")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String containerName;

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("image")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String image;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("podName")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String podName;

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    /**
     * RegistryAuth specifies credentials for pushing the committed image.
     * If nil, the commit will attempt an anonymous push.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("registryAuth")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("RegistryAuth specifies credentials for pushing the committed image.\nIf nil, the commit will attempt an anonymous push.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.commitspec.RegistryAuth registryAuth;

    public io.openkruise.agents.client.v2.models.commitspec.RegistryAuth getRegistryAuth() {
        return registryAuth;
    }

    public void setRegistryAuth(io.openkruise.agents.client.v2.models.commitspec.RegistryAuth registryAuth) {
        this.registryAuth = registryAuth;
    }

    /**
     * SquashLayer is the max number of writable layers to keep after squashing.
     * 0 means no squashing. Reserved for future implementation.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("squashLayer")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("SquashLayer is the max number of writable layers to keep after squashing.\n0 means no squashing. Reserved for future implementation.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer squashLayer = 0;

    public Integer getSquashLayer() {
        return squashLayer;
    }

    public void setSquashLayer(Integer squashLayer) {
        this.squashLayer = squashLayer;
    }

    /**
     * TimeoutSeconds is the max duration (in seconds) for the commit job.
     * Exceeded jobs are terminated and marked Failed. 0 means no timeout.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("timeoutSeconds")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TimeoutSeconds is the max duration (in seconds) for the commit job.\nExceeded jobs are terminated and marked Failed. 0 means no timeout.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer timeoutSeconds = 0;

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * TtlAfterFinished is how long the Commit is retained after reaching a terminal phase.
     * The controller auto-deletes the Commit once TTL expires. Nil means no auto-deletion.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("ttl")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TtlAfterFinished is how long the Commit is retained after reaching a terminal phase.\nThe controller auto-deletes the Commit once TTL expires. Nil means no auto-deletion.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String ttl;

    public String getTtl() {
        return ttl;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }
}

