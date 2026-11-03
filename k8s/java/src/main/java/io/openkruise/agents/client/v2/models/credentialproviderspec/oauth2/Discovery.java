package io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"authorizationEndpoint","issuer","tokenEndpoint"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Discovery implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * AuthorizationEndpoint is where the user is sent to approve access.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("authorizationEndpoint")
    @io.fabric8.generator.annotation.Required()
    @io.fabric8.generator.annotation.Pattern("^https://")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("AuthorizationEndpoint is where the user is sent to approve access.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String authorizationEndpoint;

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    /**
     * Issuer identifies the provider.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("issuer")
    @io.fabric8.generator.annotation.Required()
    @io.fabric8.generator.annotation.Pattern("^https://")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Issuer identifies the provider.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String issuer;

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * TokenEndpoint is where an authorization code is exchanged for a token.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("tokenEndpoint")
    @io.fabric8.generator.annotation.Required()
    @io.fabric8.generator.annotation.Pattern("^https://")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TokenEndpoint is where an authorization code is exchanged for a token.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String tokenEndpoint;

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }
}

