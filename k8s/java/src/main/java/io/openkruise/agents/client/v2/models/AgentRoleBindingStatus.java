package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"conditions","observedGeneration"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class AgentRoleBindingStatus implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Conditions holds the observations of the binding's state. Ready is True
     * when the current generation validated and both referenced resources are
     * themselves Ready.
     *
     * Readiness is reporting, not enforcement. Authorization reads the current
     * bindings on every request, so a binding that has not yet been observed
     * here still takes effect, and a deleted one stops granting immediately.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("conditions")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Conditions holds the observations of the binding's state. Ready is True\nwhen the current generation validated and both referenced resources are\nthemselves Ready.\n\nReadiness is reporting, not enforcement. Authorization reads the current\nbindings on every request, so a binding that has not yet been observed\nhere still takes effect, and a deleted one stops granting immediately.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.agentrolebindingstatus.Conditions> conditions;

    public java.util.List<io.openkruise.agents.client.v2.models.agentrolebindingstatus.Conditions> getConditions() {
        return conditions;
    }

    public void setConditions(java.util.List<io.openkruise.agents.client.v2.models.agentrolebindingstatus.Conditions> conditions) {
        this.conditions = conditions;
    }

    /**
     * ObservedGeneration is the most recent generation observed by the
     * controller.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("observedGeneration")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ObservedGeneration is the most recent generation observed by the\ncontroller.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Long observedGeneration;

    public Long getObservedGeneration() {
        return observedGeneration;
    }

    public void setObservedGeneration(Long observedGeneration) {
        this.observedGeneration = observedGeneration;
    }
}

