package io.openkruise.agents.client.v2.models.agentidentityspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"apiGroup","kind","name"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class AuthenticationRefs implements io.fabric8.kubernetes.api.model.KubernetesResource {

    public enum ApiGroup {

        @com.fasterxml.jackson.annotation.JsonProperty("security.agents.kruise.io")
        SECURITY_AGENTS_KRUISE_IO("security.agents.kruise.io");

        java.lang.String value;

        ApiGroup(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * APIGroup of the referent. Only security.agents.kruise.io is accepted.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("apiGroup")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("APIGroup of the referent. Only security.agents.kruise.io is accepted.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private ApiGroup apiGroup;

    public ApiGroup getApiGroup() {
        return apiGroup;
    }

    public void setApiGroup(ApiGroup apiGroup) {
        this.apiGroup = apiGroup;
    }

    public enum Kind {

        @com.fasterxml.jackson.annotation.JsonProperty("AgentAuthenticationConfig")
        AGENTAUTHENTICATIONCONFIG("AgentAuthenticationConfig");

        java.lang.String value;

        Kind(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * Kind of the referent. Only AgentAuthenticationConfig is accepted.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("kind")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Kind of the referent. Only AgentAuthenticationConfig is accepted.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Kind kind;

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    /**
     * Name of the referent in the same namespace.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Name of the referent in the same namespace.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

