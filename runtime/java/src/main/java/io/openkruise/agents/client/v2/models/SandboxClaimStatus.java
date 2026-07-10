package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"claimStartTime","claimedReplicas","completionTime","conditions","message","observedGeneration","phase"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class SandboxClaimStatus implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * ClaimStartTime is the timestamp when claiming started
     * Used for calculating timeout
     */
    @com.fasterxml.jackson.annotation.JsonProperty("claimStartTime")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ClaimStartTime is the timestamp when claiming started\nUsed for calculating timeout")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.time.ZonedDateTime claimStartTime;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssVV")
    public java.time.ZonedDateTime getClaimStartTime() {
        return claimStartTime;
    }

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[XXX][VV]")
    public void setClaimStartTime(java.time.ZonedDateTime claimStartTime) {
        this.claimStartTime = claimStartTime;
    }

    /**
     * ClaimedReplicas indicates how many sandboxes are currently claimed (total)
     * This is determined by querying sandboxes with matching ownerReference
     * Only updated during Pending and Claiming phases
     */
    @com.fasterxml.jackson.annotation.JsonProperty("claimedReplicas")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ClaimedReplicas indicates how many sandboxes are currently claimed (total)\nThis is determined by querying sandboxes with matching ownerReference\nOnly updated during Pending and Claiming phases")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer claimedReplicas;

    public Integer getClaimedReplicas() {
        return claimedReplicas;
    }

    public void setClaimedReplicas(Integer claimedReplicas) {
        this.claimedReplicas = claimedReplicas;
    }

    /**
     * CompletionTime is the timestamp when the claim reached Completed phase
     * Used for TTL calculation
     */
    @com.fasterxml.jackson.annotation.JsonProperty("completionTime")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("CompletionTime is the timestamp when the claim reached Completed phase\nUsed for TTL calculation")
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
     * Conditions represent the current state of the SandboxClaim
     */
    @com.fasterxml.jackson.annotation.JsonProperty("conditions")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Conditions represent the current state of the SandboxClaim")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.sandboxclaimstatus.Conditions> conditions;

    public java.util.List<io.openkruise.agents.client.v2.models.sandboxclaimstatus.Conditions> getConditions() {
        return conditions;
    }

    public void setConditions(java.util.List<io.openkruise.agents.client.v2.models.sandboxclaimstatus.Conditions> conditions) {
        this.conditions = conditions;
    }

    /**
     * Message provides human-readable details about the current phase
     */
    @com.fasterxml.jackson.annotation.JsonProperty("message")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Message provides human-readable details about the current phase")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * ObservedGeneration is the most recent generation observed
     */
    @com.fasterxml.jackson.annotation.JsonProperty("observedGeneration")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ObservedGeneration is the most recent generation observed")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Long observedGeneration;

    public Long getObservedGeneration() {
        return observedGeneration;
    }

    public void setObservedGeneration(Long observedGeneration) {
        this.observedGeneration = observedGeneration;
    }

    /**
     * Phase represents the current phase of the claim
     * Claiming: In the process of claiming sandboxes
     * Completed: Claim process finished (either all replicas claimed or timeout reached)
     */
    @com.fasterxml.jackson.annotation.JsonProperty("phase")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Phase represents the current phase of the claim\nClaiming: In the process of claiming sandboxes\nCompleted: Claim process finished (either all replicas claimed or timeout reached)")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String phase;

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }
}

