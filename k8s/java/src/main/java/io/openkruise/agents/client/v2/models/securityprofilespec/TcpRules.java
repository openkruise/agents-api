package io.openkruise.agents.client.v2.models.securityprofilespec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"action","match","name"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class TcpRules implements io.fabric8.kubernetes.api.model.KubernetesResource {

    public enum Action {

        @com.fasterxml.jackson.annotation.JsonProperty("Allow")
        ALLOW("Allow"), @com.fasterxml.jackson.annotation.JsonProperty("Deny")
        DENY("Deny");

        java.lang.String value;

        Action(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * Action is required and has no default. Allow permits the destination;
     * Deny rejects it. Both stop evaluation of all remaining TCP rules.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("action")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Action is required and has no default. Allow permits the destination;\nDeny rejects it. Both stop evaluation of all remaining TCP rules.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Action action;

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * Match lists alternative conditions. Entries are ORed; fields within
     * an entry are ANDed. An empty entry matches every destination.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("match")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Match lists alternative conditions. Entries are ORed; fields within\nan entry are ANDed. An empty entry matches every destination.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.tcprules.Match> match;

    public java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.tcprules.Match> getMatch() {
        return match;
    }

    public void setMatch(java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.tcprules.Match> match) {
        this.match = match;
    }

    /**
     * Name uniquely identifies the rule within the profile.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Name uniquely identifies the rule within the profile.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

