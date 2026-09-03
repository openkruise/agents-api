package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"appliedCronPolicies","conditions","currentCapacity","currentReplicas","desiredReplicas","lastScaleTime","observedGeneration","suspended"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class PoolAutoscalerStatus implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * AppliedCronPolicies is the execution status of cron policies.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("appliedCronPolicies")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("AppliedCronPolicies is the execution status of cron policies.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.poolautoscalerstatus.AppliedCronPolicies> appliedCronPolicies;

    public java.util.List<io.openkruise.agents.client.v2.models.poolautoscalerstatus.AppliedCronPolicies> getAppliedCronPolicies() {
        return appliedCronPolicies;
    }

    public void setAppliedCronPolicies(java.util.List<io.openkruise.agents.client.v2.models.poolautoscalerstatus.AppliedCronPolicies> appliedCronPolicies) {
        this.appliedCronPolicies = appliedCronPolicies;
    }

    /**
     * Conditions is the set of conditions required for this autoscaler to scale its target.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("conditions")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Conditions is the set of conditions required for this autoscaler to scale its target.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.poolautoscalerstatus.Conditions> conditions;

    public java.util.List<io.openkruise.agents.client.v2.models.poolautoscalerstatus.Conditions> getConditions() {
        return conditions;
    }

    public void setConditions(java.util.List<io.openkruise.agents.client.v2.models.poolautoscalerstatus.Conditions> conditions) {
        this.conditions = conditions;
    }

    /**
     * CurrentCapacity is the last read state of the capacity used by this autoscaler.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("currentCapacity")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("CurrentCapacity is the last read state of the capacity used by this autoscaler.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.poolautoscalerstatus.CurrentCapacity currentCapacity;

    public io.openkruise.agents.client.v2.models.poolautoscalerstatus.CurrentCapacity getCurrentCapacity() {
        return currentCapacity;
    }

    public void setCurrentCapacity(io.openkruise.agents.client.v2.models.poolautoscalerstatus.CurrentCapacity currentCapacity) {
        this.currentCapacity = currentCapacity;
    }

    /**
     * CurrentReplicas is current number of replicas of pods managed by this autoscaler.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("currentReplicas")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("CurrentReplicas is current number of replicas of pods managed by this autoscaler.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer currentReplicas;

    public Integer getCurrentReplicas() {
        return currentReplicas;
    }

    public void setCurrentReplicas(Integer currentReplicas) {
        this.currentReplicas = currentReplicas;
    }

    /**
     * DesiredReplicas is the desired number of replicas of pods managed by this autoscaler.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("desiredReplicas")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("DesiredReplicas is the desired number of replicas of pods managed by this autoscaler.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer desiredReplicas;

    public Integer getDesiredReplicas() {
        return desiredReplicas;
    }

    public void setDesiredReplicas(Integer desiredReplicas) {
        this.desiredReplicas = desiredReplicas;
    }

    /**
     * LastScaleTime is the last time the PoolAutoscaler scaled the number of pods.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("lastScaleTime")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("LastScaleTime is the last time the PoolAutoscaler scaled the number of pods.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.time.ZonedDateTime lastScaleTime;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssVV")
    public java.time.ZonedDateTime getLastScaleTime() {
        return lastScaleTime;
    }

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[XXX][VV]")
    public void setLastScaleTime(java.time.ZonedDateTime lastScaleTime) {
        this.lastScaleTime = lastScaleTime;
    }

    /**
     * ObservedGeneration is the most recent generation observed by this autoscaler.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("observedGeneration")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ObservedGeneration is the most recent generation observed by this autoscaler.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Long observedGeneration;

    public Long getObservedGeneration() {
        return observedGeneration;
    }

    public void setObservedGeneration(Long observedGeneration) {
        this.observedGeneration = observedGeneration;
    }

    /**
     * Suspended indicates whether the autoscaler is currently suspended.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("suspended")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Suspended indicates whether the autoscaler is currently suspended.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Boolean suspended;

    public Boolean getSuspended() {
        return suspended;
    }

    public void setSuspended(Boolean suspended) {
        this.suspended = suspended;
    }
}

