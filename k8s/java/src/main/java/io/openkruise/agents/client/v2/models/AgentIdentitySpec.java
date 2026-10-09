package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"authenticationRefs"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class AgentIdentitySpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * AuthenticationRefs lists the issuers allowed to authenticate end users
     * delegating to this agent.
     *
     * These are consumed when exchanging a principal token, not when issuing an
     * agent token: an agent token proves the workload and needs no end user.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("authenticationRefs")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("AuthenticationRefs lists the issuers allowed to authenticate end users\ndelegating to this agent.\n\nThese are consumed when exchanging a principal token, not when issuing an\nagent token: an agent token proves the workload and needs no end user.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.agentidentityspec.AuthenticationRefs> authenticationRefs;

    public java.util.List<io.openkruise.agents.client.v2.models.agentidentityspec.AuthenticationRefs> getAuthenticationRefs() {
        return authenticationRefs;
    }

    public void setAuthenticationRefs(java.util.List<io.openkruise.agents.client.v2.models.agentidentityspec.AuthenticationRefs> authenticationRefs) {
        this.authenticationRefs = authenticationRefs;
    }
}

