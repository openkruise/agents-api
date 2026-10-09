package io.openkruise.agents.client.v2.models.agentauthenticationconfigspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"allowedAudience","discoveryUrl"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Jwt implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * AllowedAudience lists the audiences accepted in an ID token. An ID token
     * naming none of these is rejected, which is what stops a token minted for
     * another application being replayed here.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("allowedAudience")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("AllowedAudience lists the audiences accepted in an ID token. An ID token\nnaming none of these is rejected, which is what stops a token minted for\nanother application being replayed here.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<String> allowedAudience;

    public java.util.List<String> getAllowedAudience() {
        return allowedAudience;
    }

    public void setAllowedAudience(java.util.List<String> allowedAudience) {
        this.allowedAudience = allowedAudience;
    }

    /**
     * DiscoveryURL is the OIDC discovery document of the issuer. It must be an
     * absolute HTTPS URL.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("discoveryUrl")
    @io.fabric8.generator.annotation.Required()
    @io.fabric8.generator.annotation.Pattern("^https://")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("DiscoveryURL is the OIDC discovery document of the issuer. It must be an\nabsolute HTTPS URL.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String discoveryUrl;

    public String getDiscoveryUrl() {
        return discoveryUrl;
    }

    public void setDiscoveryUrl(String discoveryUrl) {
        this.discoveryUrl = discoveryUrl;
    }
}

