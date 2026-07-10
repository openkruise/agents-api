package io.openkruise.agents.client.v2.models.securityprofilespec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"actions","match","name"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Rules implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Actions is a map of action types to their configurations. The Envoy
     * data plane executes populated actions in a deterministic order; each
     * action runs at most once. Terminal actions (Block, Bypass)
     * short-circuit the rule chain.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("actions")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Actions is a map of action types to their configurations. The Envoy\ndata plane executes populated actions in a deterministic order; each\naction runs at most once. Terminal actions (Block, Bypass)\nshort-circuit the rule chain.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.rules.Actions actions;

    public io.openkruise.agents.client.v2.models.securityprofilespec.rules.Actions getActions() {
        return actions;
    }

    public void setActions(io.openkruise.agents.client.v2.models.securityprofilespec.rules.Actions actions) {
        this.actions = actions;
    }

    /**
     * Match lists match conditions. Multiple entries are ORed.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("match")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Match lists match conditions. Multiple entries are ORed.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.Match> match;

    public java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.Match> getMatch() {
        return match;
    }

    public void setMatch(java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.Match> match) {
        this.match = match;
    }

    /**
     * Name uniquely identifies the rule within the profile. Used in
     * metrics, events, and generated xDS resource names.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Name uniquely identifies the rule within the profile. Used in\nmetrics, events, and generated xDS resource names.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

