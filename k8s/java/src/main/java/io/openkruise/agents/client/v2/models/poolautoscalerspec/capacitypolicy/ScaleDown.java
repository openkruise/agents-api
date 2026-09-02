package io.openkruise.agents.client.v2.models.poolautoscalerspec.capacitypolicy;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"stabilizationWindowSeconds"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class ScaleDown implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * StabilizationWindowSeconds is the cooldown period after any scale action before
     * a scale in this direction is allowed. This is a cooldown model: the first scale
     * action is immediate, subsequent actions must wait for the window to elapse since
     * the most recent scale action (in either direction).
     * Must be >= 60 and <= 3600 (one hour) when set.
     * Scale-up defaults to 60 seconds when omitted and is normalized at runtime to at
     * least the process-wide Sandbox Pending timeout plus 10 seconds.
     * Scale-down defaults to 300 seconds when omitted.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("stabilizationWindowSeconds")
    @io.fabric8.generator.annotation.Max(3600.0)
    @io.fabric8.generator.annotation.Min(60.0)
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("StabilizationWindowSeconds is the cooldown period after any scale action before\na scale in this direction is allowed. This is a cooldown model: the first scale\naction is immediate, subsequent actions must wait for the window to elapse since\nthe most recent scale action (in either direction).\nMust be >= 60 and <= 3600 (one hour) when set.\nScale-up defaults to 60 seconds when omitted and is normalized at runtime to at\nleast the process-wide Sandbox Pending timeout plus 10 seconds.\nScale-down defaults to 300 seconds when omitted.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer stabilizationWindowSeconds;

    public Integer getStabilizationWindowSeconds() {
        return stabilizationWindowSeconds;
    }

    public void setStabilizationWindowSeconds(Integer stabilizationWindowSeconds) {
        this.stabilizationWindowSeconds = stabilizationWindowSeconds;
    }
}

