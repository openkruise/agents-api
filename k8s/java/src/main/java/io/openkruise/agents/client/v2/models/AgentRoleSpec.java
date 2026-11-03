package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"rules"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class AgentRoleSpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Rules granting permissions. An empty list is a role that permits nothing,
     * which is valid but useless, so at least one is required to make an
     * accidentally empty role visible at admission instead of at request time.
     *
     * The action-to-kind constraints live on AgentRoleRule rather than here, so
     * a violation points at spec.rules[i].resources[j] instead of the whole list.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("rules")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Rules granting permissions. An empty list is a role that permits nothing,\nwhich is valid but useless, so at least one is required to make an\naccidentally empty role visible at admission instead of at request time.\n\nThe action-to-kind constraints live on AgentRoleRule rather than here, so\na violation points at spec.rules[i].resources[j] instead of the whole list.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.agentrolespec.Rules> rules;

    public java.util.List<io.openkruise.agents.client.v2.models.agentrolespec.Rules> getRules() {
        return rules;
    }

    public void setRules(java.util.List<io.openkruise.agents.client.v2.models.agentrolespec.Rules> rules) {
        this.rules = rules;
    }
}

