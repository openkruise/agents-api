package io.openkruise.agents.client.v2.models.sandboxsetspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"maxUnavailable"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class ScaleStrategy implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * MaxUnavailable caps concurrent physical scale-up operations and serves as
     * the startup budget for the ScalingLimited condition. It can be an absolute
     * number (ex: 5) or a percentage of desired pods (ex: 10%); percentages are
     * rounded up against spec.replicas. If unset or invalid, the controller uses
     * the base (equivalent to 100%, i.e. no cap). Scale-down is unaffected.
     *
     * The physical scale-up budget is charged by startup blockers: sandboxes
     * whose Ready condition is False with reason PodCreateFailed or
     * StartContainerFailed, sandboxes stuck in Creating/ResourcePending past the
     * configured --max-pending-timeout, and sandbox creations that have been
     * issued but are not yet observed by the controller (they release their slot
     * once observed as healthy Creating sandboxes). Healthy observed Creating
     * sandboxes do NOT count against the budget.
     *
     * The ScalingLimited condition becomes True with reason
     * StartupBudgetExhausted when failed plus pending-timeout sandboxes exhaust
     * the resolved startup budget.
     * MaxUnavailable works only for scale-up.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("maxUnavailable")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("MaxUnavailable caps concurrent physical scale-up operations and serves as\nthe startup budget for the ScalingLimited condition. It can be an absolute\nnumber (ex: 5) or a percentage of desired pods (ex: 10%); percentages are\nrounded up against spec.replicas. If unset or invalid, the controller uses\nthe base (equivalent to 100%, i.e. no cap). Scale-down is unaffected.\n\nThe physical scale-up budget is charged by startup blockers: sandboxes\nwhose Ready condition is False with reason PodCreateFailed or\nStartContainerFailed, sandboxes stuck in Creating/ResourcePending past the\nconfigured --max-pending-timeout, and sandbox creations that have been\nissued but are not yet observed by the controller (they release their slot\nonce observed as healthy Creating sandboxes). Healthy observed Creating\nsandboxes do NOT count against the budget.\n\nThe ScalingLimited condition becomes True with reason\nStartupBudgetExhausted when failed plus pending-timeout sandboxes exhaust\nthe resolved startup budget.\nMaxUnavailable works only for scale-up.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.fabric8.kubernetes.api.model.IntOrString maxUnavailable;

    public io.fabric8.kubernetes.api.model.IntOrString getMaxUnavailable() {
        return maxUnavailable;
    }

    public void setMaxUnavailable(io.fabric8.kubernetes.api.model.IntOrString maxUnavailable) {
        this.maxUnavailable = maxUnavailable;
    }
}

