package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"keepRunning","persistentContents","podName","sandboxName","ttlAfterFinished"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class CheckpointSpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * KeepRunning indicates whether the pod remains in the Running state after passing the checkpoint.
     * Default is true.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("keepRunning")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("KeepRunning indicates whether the pod remains in the Running state after passing the checkpoint.\nDefault is true.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Boolean keepRunning;

    public Boolean getKeepRunning() {
        return keepRunning;
    }

    public void setKeepRunning(Boolean keepRunning) {
        this.keepRunning = keepRunning;
    }

    /**
     * PersistentContents indicates resume pod with persistent content, Enum: memory, filesystem
     */
    @com.fasterxml.jackson.annotation.JsonProperty("persistentContents")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("PersistentContents indicates resume pod with persistent content, Enum: memory, filesystem")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<String> persistentContents;

    public java.util.List<String> getPersistentContents() {
        return persistentContents;
    }

    public void setPersistentContents(java.util.List<String> persistentContents) {
        this.persistentContents = persistentContents;
    }

    /**
     * PodName is checkpoint podName.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("podName")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("PodName is checkpoint podName.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String podName;

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    /**
     * SandboxName is checkpoint sandboxName.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("sandboxName")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("SandboxName is checkpoint sandboxName.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String sandboxName;

    public String getSandboxName() {
        return sandboxName;
    }

    public void setSandboxName(String sandboxName) {
        this.sandboxName = sandboxName;
    }

    /**
     * valid format: 30s, 30m, 30d
     */
    @com.fasterxml.jackson.annotation.JsonProperty("ttlAfterFinished")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("valid format: 30s, 30m, 30d")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String ttlAfterFinished;

    public String getTtlAfterFinished() {
        return ttlAfterFinished;
    }

    public void setTtlAfterFinished(String ttlAfterFinished) {
        this.ttlAfterFinished = ttlAfterFinished;
    }
}

