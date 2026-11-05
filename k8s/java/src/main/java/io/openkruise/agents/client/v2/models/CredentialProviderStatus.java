package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"callbackUrl","conditions","observedGeneration"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class CredentialProviderStatus implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * CallbackURL is callbackBaseUrl with CallbackPath appended. It is derived
     * rather than configured so that what an operator registers with the third
     * party is exactly what the server will accept, with no chance of the two
     * drifting apart.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("callbackUrl")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("CallbackURL is callbackBaseUrl with CallbackPath appended. It is derived\nrather than configured so that what an operator registers with the third\nparty is exactly what the server will accept, with no chance of the two\ndrifting apart.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String callbackUrl;

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    /**
     * Conditions holds the observations of the provider's state. Ready is True
     * when the current generation validated and the client secret was loaded.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("conditions")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Conditions holds the observations of the provider's state. Ready is True\nwhen the current generation validated and the client secret was loaded.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.credentialproviderstatus.Conditions> conditions;

    public java.util.List<io.openkruise.agents.client.v2.models.credentialproviderstatus.Conditions> getConditions() {
        return conditions;
    }

    public void setConditions(java.util.List<io.openkruise.agents.client.v2.models.credentialproviderstatus.Conditions> conditions) {
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

