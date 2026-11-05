package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"artifacts","conditions","observedGeneration","phase"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class SandboxTemplateStatus implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Artifacts contains at most one immutable reference of each artifact type.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("artifacts")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Artifacts contains at most one immutable reference of each artifact type.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.sandboxtemplatestatus.Artifacts> artifacts;

    public java.util.List<io.openkruise.agents.client.v2.models.sandboxtemplatestatus.Artifacts> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(java.util.List<io.openkruise.agents.client.v2.models.sandboxtemplatestatus.Artifacts> artifacts) {
        this.artifacts = artifacts;
    }

    /**
     * Conditions report build progress, readiness, and sanitized failures.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("conditions")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Conditions report build progress, readiness, and sanitized failures.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.sandboxtemplatestatus.Conditions> conditions;

    public java.util.List<io.openkruise.agents.client.v2.models.sandboxtemplatestatus.Conditions> getConditions() {
        return conditions;
    }

    public void setConditions(java.util.List<io.openkruise.agents.client.v2.models.sandboxtemplatestatus.Conditions> conditions) {
        this.conditions = conditions;
    }

    /**
     * ObservedGeneration is the most recent generation processed by the
     * controller.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("observedGeneration")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ObservedGeneration is the most recent generation processed by the\ncontroller.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Long observedGeneration;

    public Long getObservedGeneration() {
        return observedGeneration;
    }

    public void setObservedGeneration(Long observedGeneration) {
        this.observedGeneration = observedGeneration;
    }

    public enum Phase {

        @com.fasterxml.jackson.annotation.JsonProperty("Pending")
        PENDING("Pending"), @com.fasterxml.jackson.annotation.JsonProperty("Building")
        BUILDING("Building"), @com.fasterxml.jackson.annotation.JsonProperty("Ready")
        READY("Ready"), @com.fasterxml.jackson.annotation.JsonProperty("Failed")
        FAILED("Failed");

        java.lang.String value;

        Phase(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * Phase is the overall Template Build lifecycle phase.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("phase")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Phase is the overall Template Build lifecycle phase.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Phase phase;

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }
}

