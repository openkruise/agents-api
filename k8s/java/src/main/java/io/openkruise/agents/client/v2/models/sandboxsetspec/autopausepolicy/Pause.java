package io.openkruise.agents.client.v2.models.sandboxsetspec.autopausepolicy;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"whenProbedIdleState"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Pause implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * WhenProbedIdleState pauses the sandbox when a probe's Condition message
     * matches MessageRegex for at least ThresholdDuration.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("whenProbedIdleState")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("WhenProbedIdleState pauses the sandbox when a probe's Condition message\nmatches MessageRegex for at least ThresholdDuration.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxsetspec.autopausepolicy.pause.WhenProbedIdleState whenProbedIdleState;

    public io.openkruise.agents.client.v2.models.sandboxsetspec.autopausepolicy.pause.WhenProbedIdleState getWhenProbedIdleState() {
        return whenProbedIdleState;
    }

    public void setWhenProbedIdleState(io.openkruise.agents.client.v2.models.sandboxsetspec.autopausepolicy.pause.WhenProbedIdleState whenProbedIdleState) {
        this.whenProbedIdleState = whenProbedIdleState;
    }
}

