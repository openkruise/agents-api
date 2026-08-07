package io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"defaultAction","denyResponse","rules","unsupportedVersionAction"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class McpToolPolicy implements io.fabric8.kubernetes.api.model.KubernetesResource {

    public enum DefaultAction {

        @com.fasterxml.jackson.annotation.JsonProperty("allow")
        ALLOW("allow"), @com.fasterxml.jackson.annotation.JsonProperty("deny")
        DENY("deny");

        java.lang.String value;

        DefaultAction(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * DefaultAction is the decision applied when no rule matches.
     * Defaults to "deny" (whitelist mode); set "allow" for blacklist mode.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("defaultAction")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("DefaultAction is the decision applied when no rule matches.\nDefaults to \"deny\" (whitelist mode); set \"allow\" for blacklist mode.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private DefaultAction defaultAction = io.fabric8.kubernetes.client.utils.Serialization.unmarshal("\"deny\"", DefaultAction.class);

    public DefaultAction getDefaultAction() {
        return defaultAction;
    }

    public void setDefaultAction(DefaultAction defaultAction) {
        this.defaultAction = defaultAction;
    }

    /**
     * DenyResponse configures the HTTP response when a tool is denied.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("denyResponse")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("DenyResponse configures the HTTP response when a tool is denied.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.mcptoolpolicy.DenyResponse denyResponse;

    public io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.mcptoolpolicy.DenyResponse getDenyResponse() {
        return denyResponse;
    }

    public void setDenyResponse(io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.mcptoolpolicy.DenyResponse denyResponse) {
        this.denyResponse = denyResponse;
    }

    /**
     * Rules are evaluated in order. First match wins.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("rules")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Rules are evaluated in order. First match wins.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.mcptoolpolicy.Rules> rules;

    public java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.mcptoolpolicy.Rules> getRules() {
        return rules;
    }

    public void setRules(java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.mcptoolpolicy.Rules> rules) {
        this.rules = rules;
    }

    public enum UnsupportedVersionAction {

        @com.fasterxml.jackson.annotation.JsonProperty("deny")
        DENY("deny"), @com.fasterxml.jackson.annotation.JsonProperty("passthrough")
        PASSTHROUGH("passthrough");

        java.lang.String value;

        UnsupportedVersionAction(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * UnsupportedVersionAction determines how a tools/call request with a
     * missing or unsupported MCP-Protocol-Version header is handled. "deny"
     * rejects the request, while "passthrough" skips policy evaluation.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("unsupportedVersionAction")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("UnsupportedVersionAction determines how a tools/call request with a\nmissing or unsupported MCP-Protocol-Version header is handled. \"deny\"\nrejects the request, while \"passthrough\" skips policy evaluation.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private UnsupportedVersionAction unsupportedVersionAction = io.fabric8.kubernetes.client.utils.Serialization.unmarshal("\"deny\"", UnsupportedVersionAction.class);

    public UnsupportedVersionAction getUnsupportedVersionAction() {
        return unsupportedVersionAction;
    }

    public void setUnsupportedVersionAction(UnsupportedVersionAction unsupportedVersionAction) {
        this.unsupportedVersionAction = unsupportedVersionAction;
    }
}

