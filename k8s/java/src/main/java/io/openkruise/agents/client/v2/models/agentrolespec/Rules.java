package io.openkruise.agents.client.v2.models.agentrolespec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"actions","resources"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Rules implements io.fabric8.kubernetes.api.model.KubernetesResource {

    public enum Actions {

        @com.fasterxml.jackson.annotation.JsonProperty("ExchangePrincipalToken")
        EXCHANGEPRINCIPALTOKEN("ExchangePrincipalToken"), @com.fasterxml.jackson.annotation.JsonProperty("GetResourceOAuth2Token")
        GETRESOURCEOAUTH2TOKEN("GetResourceOAuth2Token");

        java.lang.String value;

        Actions(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * Actions permitted by this rule.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("actions")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Actions permitted by this rule.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<Actions> actions;

    public java.util.List<Actions> getActions() {
        return actions;
    }

    public void setActions(java.util.List<Actions> actions) {
        this.actions = actions;
    }

    /**
     * Resources the actions apply to, as "*" or "<Kind>/<name>".
     *
     * MaxLength is the 19-character "CredentialProvider/" prefix plus a
     * 253-character name. The pattern rejects a bare name, an unknown kind and
     * anything that is not a DNS name after the prefix, including a wildcard in
     * the name position.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("resources")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Resources the actions apply to, as \"*\" or \"<Kind>/<name>\".\n\nMaxLength is the 19-character \"CredentialProvider/\" prefix plus a\n253-character name. The pattern rejects a bare name, an unknown kind and\nanything that is not a DNS name after the prefix, including a wildcard in\nthe name position.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<String> resources;

    public java.util.List<String> getResources() {
        return resources;
    }

    public void setResources(java.util.List<String> resources) {
        this.resources = resources;
    }
}

