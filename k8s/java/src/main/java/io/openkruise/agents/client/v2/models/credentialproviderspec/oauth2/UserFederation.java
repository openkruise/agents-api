package io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"callbackBaseUrl","defaultScopes"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class UserFederation implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * CallbackBaseURL is the externally reachable base the callback is served
     * under. The controller appends CallbackPath to form status.callbackUrl,
     * which is what gets registered with the third party.
     *
     * A query, fragment or user information here would produce a redirect URI
     * that cannot match the registration, so they are rejected rather than
     * silently dropped.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("callbackBaseUrl")
    @io.fabric8.generator.annotation.Required()
    @io.fabric8.generator.annotation.Pattern("^https://[^/?#@]+(/[^?#]*)?$")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("CallbackBaseURL is the externally reachable base the callback is served\nunder. The controller appends CallbackPath to form status.callbackUrl,\nwhich is what gets registered with the third party.\n\nA query, fragment or user information here would produce a redirect URI\nthat cannot match the registration, so they are rejected rather than\nsilently dropped.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String callbackBaseUrl;

    public String getCallbackBaseUrl() {
        return callbackBaseUrl;
    }

    public void setCallbackBaseUrl(String callbackBaseUrl) {
        this.callbackBaseUrl = callbackBaseUrl;
    }

    /**
     * DefaultScopes are requested when a caller names none. A caller may request
     * a subset but never more, so this is the ceiling on what any agent in this
     * namespace can be granted through this provider.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("defaultScopes")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("DefaultScopes are requested when a caller names none. A caller may request\na subset but never more, so this is the ceiling on what any agent in this\nnamespace can be granted through this provider.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<String> defaultScopes;

    public java.util.List<String> getDefaultScopes() {
        return defaultScopes;
    }

    public void setDefaultScopes(java.util.List<String> defaultScopes) {
        this.defaultScopes = defaultScopes;
    }
}

