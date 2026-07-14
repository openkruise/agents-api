package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"conditions","observedGeneration"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class SecurityProfileStatus implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Conditions summarizes the profile's current state. Standard types are
     * Accepted and Programmed (see SecurityProfileCondition* constants).
     */
    @com.fasterxml.jackson.annotation.JsonProperty("conditions")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Conditions summarizes the profile's current state. Standard types are\nAccepted and Programmed (see SecurityProfileCondition* constants).")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.securityprofilestatus.Conditions> conditions;

    public java.util.List<io.openkruise.agents.client.v2.models.securityprofilestatus.Conditions> getConditions() {
        return conditions;
    }

    public void setConditions(java.util.List<io.openkruise.agents.client.v2.models.securityprofilestatus.Conditions> conditions) {
        this.conditions = conditions;
    }

    /**
     * ObservedGeneration is the .metadata.generation last reconciled.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("observedGeneration")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ObservedGeneration is the .metadata.generation last reconciled.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Long observedGeneration;

    public Long getObservedGeneration() {
        return observedGeneration;
    }

    public void setObservedGeneration(Long observedGeneration) {
        this.observedGeneration = observedGeneration;
    }
}

