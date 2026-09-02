package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"capacityPolicy","cronPolicies","maxReplicas","minReplicas","scaleTargetRef","suspend"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class PoolAutoscalerSpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * CapacityPolicy defines the capacity configuration of the target resource pool.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("capacityPolicy")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("CapacityPolicy defines the capacity configuration of the target resource pool.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.poolautoscalerspec.CapacityPolicy capacityPolicy;

    public io.openkruise.agents.client.v2.models.poolautoscalerspec.CapacityPolicy getCapacityPolicy() {
        return capacityPolicy;
    }

    public void setCapacityPolicy(io.openkruise.agents.client.v2.models.poolautoscalerspec.CapacityPolicy capacityPolicy) {
        this.capacityPolicy = capacityPolicy;
    }

    /**
     * CronPolicies is a list of potential cron scaling policies which can be used during scaling.
     * When both CronPolicies and CapacityPolicy are set, CronPolicies takes higher priority.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("cronPolicies")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("CronPolicies is a list of potential cron scaling policies which can be used during scaling.\nWhen both CronPolicies and CapacityPolicy are set, CronPolicies takes higher priority.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.poolautoscalerspec.CronPolicies> cronPolicies;

    public java.util.List<io.openkruise.agents.client.v2.models.poolautoscalerspec.CronPolicies> getCronPolicies() {
        return cronPolicies;
    }

    public void setCronPolicies(java.util.List<io.openkruise.agents.client.v2.models.poolautoscalerspec.CronPolicies> cronPolicies) {
        this.cronPolicies = cronPolicies;
    }

    /**
     * MaxReplicas is the upper limit for the number of replicas to which the autoscaler can scale up.
     * It cannot be less than minReplicas.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("maxReplicas")
    @io.fabric8.generator.annotation.Required()
    @io.fabric8.generator.annotation.Min(1.0)
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("MaxReplicas is the upper limit for the number of replicas to which the autoscaler can scale up.\nIt cannot be less than minReplicas.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer maxReplicas;

    public Integer getMaxReplicas() {
        return maxReplicas;
    }

    public void setMaxReplicas(Integer maxReplicas) {
        this.maxReplicas = maxReplicas;
    }

    /**
     * MinReplicas is the lower limit for the number of replicas to which the autoscaler
     * can scale down. It defaults to 0 pods.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("minReplicas")
    @io.fabric8.generator.annotation.Min(0.0)
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("MinReplicas is the lower limit for the number of replicas to which the autoscaler\ncan scale down. It defaults to 0 pods.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer minReplicas = 0;

    public Integer getMinReplicas() {
        return minReplicas;
    }

    public void setMinReplicas(Integer minReplicas) {
        this.minReplicas = minReplicas;
    }

    /**
     * ScaleTargetRef points to the target warming pool to scale, and is used to select the pods for which instance status
     * should be collected, as well as to actually change the replica count.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("scaleTargetRef")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ScaleTargetRef points to the target warming pool to scale, and is used to select the pods for which instance status\nshould be collected, as well as to actually change the replica count.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.poolautoscalerspec.ScaleTargetRef scaleTargetRef;

    public io.openkruise.agents.client.v2.models.poolautoscalerspec.ScaleTargetRef getScaleTargetRef() {
        return scaleTargetRef;
    }

    public void setScaleTargetRef(io.openkruise.agents.client.v2.models.poolautoscalerspec.ScaleTargetRef scaleTargetRef) {
        this.scaleTargetRef = scaleTargetRef;
    }

    /**
     * Suspend tells the controller to suspend subsequent executions.
     * Defaults to false.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("suspend")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Suspend tells the controller to suspend subsequent executions.\nDefaults to false.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Boolean suspend;

    public Boolean getSuspend() {
        return suspend;
    }

    public void setSuspend(Boolean suspend) {
        this.suspend = suspend;
    }
}

