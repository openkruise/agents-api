package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"conditions"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class TrafficPolicyStatus implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Conditions summarises the policy's current state. Standard types are
     * Accepted and Programmed (see TrafficPolicyCondition* constants).
     */
    @com.fasterxml.jackson.annotation.JsonProperty("conditions")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Conditions summarises the policy's current state. Standard types are\nAccepted and Programmed (see TrafficPolicyCondition* constants).")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.trafficpolicystatus.Conditions> conditions;

    public java.util.List<io.openkruise.agents.client.v2.models.trafficpolicystatus.Conditions> getConditions() {
        return conditions;
    }

    public void setConditions(java.util.List<io.openkruise.agents.client.v2.models.trafficpolicystatus.Conditions> conditions) {
        this.conditions = conditions;
    }
}

