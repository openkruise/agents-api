package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"oauth2","type"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class CredentialProviderSpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * OAuth2 configures the third-party OAuth client. Required when Type is
     * oauth2.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("oauth2")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("OAuth2 configures the third-party OAuth client. Required when Type is\noauth2.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.credentialproviderspec.Oauth2 oauth2;

    public io.openkruise.agents.client.v2.models.credentialproviderspec.Oauth2 getOauth2() {
        return oauth2;
    }

    public void setOauth2(io.openkruise.agents.client.v2.models.credentialproviderspec.Oauth2 oauth2) {
        this.oauth2 = oauth2;
    }

    public enum Type {

        @com.fasterxml.jackson.annotation.JsonProperty("oauth2")
        OAUTH2("oauth2");

        java.lang.String value;

        Type(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * Type selects the credential mechanism. The only supported value is
     * "oauth2".
     */
    @com.fasterxml.jackson.annotation.JsonProperty("type")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Type selects the credential mechanism. The only supported value is\n\"oauth2\".")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}

