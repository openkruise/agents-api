package io.openkruise.agents.client.v2.models.poolautoscalerspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"scaleDown","scaleUp","targetAvailable","tolerance"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class CapacityPolicy implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * ScaleDown is the scaling rule for scaling down.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("scaleDown")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ScaleDown is the scaling rule for scaling down.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.poolautoscalerspec.capacitypolicy.ScaleDown scaleDown;

    public io.openkruise.agents.client.v2.models.poolautoscalerspec.capacitypolicy.ScaleDown getScaleDown() {
        return scaleDown;
    }

    public void setScaleDown(io.openkruise.agents.client.v2.models.poolautoscalerspec.capacitypolicy.ScaleDown scaleDown) {
        this.scaleDown = scaleDown;
    }

    /**
     * ScaleUp is the scaling rule for scaling up.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("scaleUp")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ScaleUp is the scaling rule for scaling up.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.poolautoscalerspec.capacitypolicy.ScaleUp scaleUp;

    public io.openkruise.agents.client.v2.models.poolautoscalerspec.capacitypolicy.ScaleUp getScaleUp() {
        return scaleUp;
    }

    public void setScaleUp(io.openkruise.agents.client.v2.models.poolautoscalerspec.capacitypolicy.ScaleUp scaleUp) {
        this.scaleUp = scaleUp;
    }

    /**
     * TargetAvailable is the desired available replicas.
     * Can be an absolute number (ex: 5) or a percentage of current replicas (ex: 70%).
     * When the pool is empty, a percentage target is bootstrapped against
     * maxReplicas so the pool can seed its initial idle capacity.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("targetAvailable")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TargetAvailable is the desired available replicas.\nCan be an absolute number (ex: 5) or a percentage of current replicas (ex: 70%).\nWhen the pool is empty, a percentage target is bootstrapped against\nmaxReplicas so the pool can seed its initial idle capacity.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.fabric8.kubernetes.api.model.IntOrString targetAvailable;

    public io.fabric8.kubernetes.api.model.IntOrString getTargetAvailable() {
        return targetAvailable;
    }

    public void setTargetAvailable(io.fabric8.kubernetes.api.model.IntOrString targetAvailable) {
        this.targetAvailable = targetAvailable;
    }

    /**
     * Tolerance is the tolerance between the watermark and desired value under which
     * no updates are made to the desired number of replicas.
     * Can be an absolute number (ex: 5) or a percentage (ex: 10%).
     * If not set, defaults to 10%.
     * A percentage tolerance is always resolved against the target value:
     * when targetAvailable is also a percentage, both percentages are combined
     * first and applied to the pool size; when targetAvailable is an absolute
     * number, the percentage tolerance is applied to that resolved target
     * (e.g. targetAvailable=5 with tolerance=10% yields watermarks [4, 6]).
     */
    @com.fasterxml.jackson.annotation.JsonProperty("tolerance")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Tolerance is the tolerance between the watermark and desired value under which\nno updates are made to the desired number of replicas.\nCan be an absolute number (ex: 5) or a percentage (ex: 10%).\nIf not set, defaults to 10%.\nA percentage tolerance is always resolved against the target value:\nwhen targetAvailable is also a percentage, both percentages are combined\nfirst and applied to the pool size; when targetAvailable is an absolute\nnumber, the percentage tolerance is applied to that resolved target\n(e.g. targetAvailable=5 with tolerance=10% yields watermarks [4, 6]).")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.fabric8.kubernetes.api.model.IntOrString tolerance;

    public io.fabric8.kubernetes.api.model.IntOrString getTolerance() {
        return tolerance;
    }

    public void setTolerance(io.fabric8.kubernetes.api.model.IntOrString tolerance) {
        this.tolerance = tolerance;
    }
}

