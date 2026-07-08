package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"checkpointId","completionTime","message","observedGeneration","phase","podTemplateDelta"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class CheckpointStatus implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * checkpoint-id
     */
    @com.fasterxml.jackson.annotation.JsonProperty("checkpointId")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("checkpoint-id")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String checkpointId;

    public String getCheckpointId() {
        return checkpointId;
    }

    public void setCheckpointId(String checkpointId) {
        this.checkpointId = checkpointId;
    }

    /**
     * CompletionTime is checkpoint completed time, and phase is Succeeded or Failed.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("completionTime")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("CompletionTime is checkpoint completed time, and phase is Succeeded or Failed.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.time.ZonedDateTime completionTime;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssVV")
    public java.time.ZonedDateTime getCompletionTime() {
        return completionTime;
    }

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[XXX][VV]")
    public void setCompletionTime(java.time.ZonedDateTime completionTime) {
        this.completionTime = completionTime;
    }

    /**
     * message
     */
    @com.fasterxml.jackson.annotation.JsonProperty("message")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("message")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * observedGeneration is the most recent generation observed for this Checkpoint. It corresponds to the
     * Checkpoint's generation, which is updated on mutation by the API Server.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("observedGeneration")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("observedGeneration is the most recent generation observed for this Checkpoint. It corresponds to the\nCheckpoint's generation, which is updated on mutation by the API Server.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Long observedGeneration;

    public Long getObservedGeneration() {
        return observedGeneration;
    }

    public void setObservedGeneration(Long observedGeneration) {
        this.observedGeneration = observedGeneration;
    }

    /**
     * Checkpoint Phase
     */
    @com.fasterxml.jackson.annotation.JsonProperty("phase")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Checkpoint Phase")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String phase;

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    /**
     * PodTemplateDelta stores a Strategic Merge Patch that captures the delta between
     * the running Pod at pause time and the base Pod generated from sandbox.spec.template
     */
    @com.fasterxml.jackson.annotation.JsonProperty("podTemplateDelta")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("PodTemplateDelta stores a Strategic Merge Patch that captures the delta between\nthe running Pod at pause time and the base Pod generated from sandbox.spec.template")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.fabric8.kubernetes.api.model.AnyType podTemplateDelta;

    public io.fabric8.kubernetes.api.model.AnyType getPodTemplateDelta() {
        return podTemplateDelta;
    }

    public void setPodTemplateDelta(io.fabric8.kubernetes.api.model.AnyType podTemplateDelta) {
        this.podTemplateDelta = podTemplateDelta;
    }
}

