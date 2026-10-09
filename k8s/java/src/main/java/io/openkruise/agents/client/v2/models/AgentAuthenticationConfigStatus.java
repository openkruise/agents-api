package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"conditions","issuer","jwksUri","observedGeneration"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class AgentAuthenticationConfigStatus implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Conditions holds the observations of the config's state. Ready is True
     * when the current generation validated and discovery succeeded.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("conditions")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Conditions holds the observations of the config's state. Ready is True\nwhen the current generation validated and discovery succeeded.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.agentauthenticationconfigstatus.Conditions> conditions;

    public java.util.List<io.openkruise.agents.client.v2.models.agentauthenticationconfigstatus.Conditions> getConditions() {
        return conditions;
    }

    public void setConditions(java.util.List<io.openkruise.agents.client.v2.models.agentauthenticationconfigstatus.Conditions> conditions) {
        this.conditions = conditions;
    }

    /**
     * Issuer is the issuer identifier read from the discovery document. It is
     * resolved rather than configured, and an ID token must match it exactly.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("issuer")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Issuer is the issuer identifier read from the discovery document. It is\nresolved rather than configured, and an ID token must match it exactly.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String issuer;

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * JWKSURI is the key set location read from the discovery document.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("jwksUri")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("JWKSURI is the key set location read from the discovery document.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String jwksUri;

    public String getJwksUri() {
        return jwksUri;
    }

    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
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

