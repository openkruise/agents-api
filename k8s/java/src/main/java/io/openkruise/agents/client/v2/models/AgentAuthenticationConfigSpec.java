package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"jwt","type"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class AgentAuthenticationConfigSpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * JWT configures the trusted OIDC issuer. Required when Type is JWT.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("jwt")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("JWT configures the trusted OIDC issuer. Required when Type is JWT.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.agentauthenticationconfigspec.Jwt jwt;

    public io.openkruise.agents.client.v2.models.agentauthenticationconfigspec.Jwt getJwt() {
        return jwt;
    }

    public void setJwt(io.openkruise.agents.client.v2.models.agentauthenticationconfigspec.Jwt jwt) {
        this.jwt = jwt;
    }

    public enum Type {

        @com.fasterxml.jackson.annotation.JsonProperty("jwt")
        JWT("jwt");

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
     * Type selects how an issuer proves an end-user identity. The only
     * supported value is "jwt": an OIDC ID token validated against a
     * discovered issuer and JWKS.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("type")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Type selects how an issuer proves an end-user identity. The only\nsupported value is \"jwt\": an OIDC ID token validated against a\ndiscovered issuer and JWKS.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}

