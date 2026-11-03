package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"agentIdentityRef","agentRoleRef"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class AgentRoleBindingSpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * AgentIdentityRef is the identity receiving them.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("agentIdentityRef")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("AgentIdentityRef is the identity receiving them.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.agentrolebindingspec.AgentIdentityRef agentIdentityRef;

    public io.openkruise.agents.client.v2.models.agentrolebindingspec.AgentIdentityRef getAgentIdentityRef() {
        return agentIdentityRef;
    }

    public void setAgentIdentityRef(io.openkruise.agents.client.v2.models.agentrolebindingspec.AgentIdentityRef agentIdentityRef) {
        this.agentIdentityRef = agentIdentityRef;
    }

    /**
     * AgentRoleRef is the role whose permissions are granted.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("agentRoleRef")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("AgentRoleRef is the role whose permissions are granted.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.agentrolebindingspec.AgentRoleRef agentRoleRef;

    public io.openkruise.agents.client.v2.models.agentrolebindingspec.AgentRoleRef getAgentRoleRef() {
        return agentRoleRef;
    }

    public void setAgentRoleRef(io.openkruise.agents.client.v2.models.agentrolebindingspec.AgentRoleRef agentRoleRef) {
        this.agentRoleRef = agentRoleRef;
    }
}

