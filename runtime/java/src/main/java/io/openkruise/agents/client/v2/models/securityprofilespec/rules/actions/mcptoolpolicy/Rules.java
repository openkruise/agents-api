package io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.mcptoolpolicy;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"action","method","toolNames"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Rules implements io.fabric8.kubernetes.api.model.KubernetesResource {

    public enum Action {

        @com.fasterxml.jackson.annotation.JsonProperty("allow")
        ALLOW("allow"), @com.fasterxml.jackson.annotation.JsonProperty("deny")
        DENY("deny");

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
     * Action: "allow" or "deny".
     */
    @com.fasterxml.jackson.annotation.JsonProperty("action")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Action: \"allow\" or \"deny\".")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Action action;

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * Method is the JSON-RPC method (e.g. "tools/call", "tools/list").
     */
    @com.fasterxml.jackson.annotation.JsonProperty("method")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Method is the JSON-RPC method (e.g. \"tools/call\", \"tools/list\").")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String method;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * ToolNames matches params.name for tools/call. Empty = any tool for this method.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("toolNames")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ToolNames matches params.name for tools/call. Empty = any tool for this method.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<String> toolNames;

    public java.util.List<String> getToolNames() {
        return toolNames;
    }

    public void setToolNames(java.util.List<String> toolNames) {
        this.toolNames = toolNames;
    }
}

