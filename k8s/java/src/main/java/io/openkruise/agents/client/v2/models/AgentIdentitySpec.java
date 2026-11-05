package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"authenticationRefs"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class AgentIdentitySpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * AuthenticationRefs lists the issuers allowed to authenticate end users
     * delegating to this agent. At least one is required.
     *
     * An identity that names no issuer cannot take part in a principal token
     * exchange, so it is not usable and must not report Ready. The constraint is
     * declared here rather than left to the controller so the API server rejects
     * an empty list outright, but the controller still fails an existing object
     * closed: an object created before this constraint can carry an empty list
     * that admission never saw.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("authenticationRefs")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("AuthenticationRefs lists the issuers allowed to authenticate end users\ndelegating to this agent. At least one is required.\n\nAn identity that names no issuer cannot take part in a principal token\nexchange, so it is not usable and must not report Ready. The constraint is\ndeclared here rather than left to the controller so the API server rejects\nan empty list outright, but the controller still fails an existing object\nclosed: an object created before this constraint can carry an empty list\nthat admission never saw.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.agentidentityspec.AuthenticationRefs> authenticationRefs;

    public java.util.List<io.openkruise.agents.client.v2.models.agentidentityspec.AuthenticationRefs> getAuthenticationRefs() {
        return authenticationRefs;
    }

    public void setAuthenticationRefs(java.util.List<io.openkruise.agents.client.v2.models.agentidentityspec.AuthenticationRefs> authenticationRefs) {
        this.authenticationRefs = authenticationRefs;
    }
}

