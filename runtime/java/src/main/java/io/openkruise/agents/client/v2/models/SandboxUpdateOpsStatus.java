package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"conditions","failedReplicas","observedGeneration","phase","replicas","updatedReplicas","updatingReplicas"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class SandboxUpdateOpsStatus implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Conditions represents the latest available observations.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("conditions")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Conditions represents the latest available observations.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.sandboxupdateopsstatus.Conditions> conditions;

    public java.util.List<io.openkruise.agents.client.v2.models.sandboxupdateopsstatus.Conditions> getConditions() {
        return conditions;
    }

    public void setConditions(java.util.List<io.openkruise.agents.client.v2.models.sandboxupdateopsstatus.Conditions> conditions) {
        this.conditions = conditions;
    }

    /**
     * FailedReplicas is the number of sandboxes that failed to update.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("failedReplicas")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("FailedReplicas is the number of sandboxes that failed to update.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer failedReplicas;

    public Integer getFailedReplicas() {
        return failedReplicas;
    }

    public void setFailedReplicas(Integer failedReplicas) {
        this.failedReplicas = failedReplicas;
    }

    /**
     * ObservedGeneration is the most recent generation observed.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("observedGeneration")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ObservedGeneration is the most recent generation observed.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Long observedGeneration;

    public Long getObservedGeneration() {
        return observedGeneration;
    }

    public void setObservedGeneration(Long observedGeneration) {
        this.observedGeneration = observedGeneration;
    }

    /**
     * Phase is the current phase of the update operation.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("phase")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Phase is the current phase of the update operation.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String phase;

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    /**
     * Replicas is the total number of sandboxes selected for update.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("replicas")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Replicas is the total number of sandboxes selected for update.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer replicas;

    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    /**
     * UpdatedReplicas is the number of sandboxes that have been successfully updated.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("updatedReplicas")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("UpdatedReplicas is the number of sandboxes that have been successfully updated.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer updatedReplicas;

    public Integer getUpdatedReplicas() {
        return updatedReplicas;
    }

    public void setUpdatedReplicas(Integer updatedReplicas) {
        this.updatedReplicas = updatedReplicas;
    }

    /**
     * UpdatingReplicas is the number of sandboxes currently being updated.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("updatingReplicas")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("UpdatingReplicas is the number of sandboxes currently being updated.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer updatingReplicas;

    public Integer getUpdatingReplicas() {
        return updatingReplicas;
    }

    public void setUpdatingReplicas(Integer updatingReplicas) {
        this.updatingReplicas = updatingReplicas;
    }
}

