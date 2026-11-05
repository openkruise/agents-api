package io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"failStrategy","provider","request","response"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class HttpCallout implements io.fabric8.kubernetes.api.model.KubernetesResource {

    public enum FailStrategy {

        @com.fasterxml.jackson.annotation.JsonProperty("Allow")
        ALLOW("Allow"), @com.fasterxml.jackson.annotation.JsonProperty("Block")
        BLOCK("Block"), @com.fasterxml.jackson.annotation.JsonProperty("Ignore")
        IGNORE("Ignore");

        java.lang.String value;

        FailStrategy(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * FailStrategy controls execution failures, including provider lookup,
     * transport, timeout, body-disclosure, and protocol errors. Defaults to
     * Block. Allow and Ignore skip the failed callout's mutations and continue.
     * A valid immediate response from the provider is always honored.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("failStrategy")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("FailStrategy controls execution failures, including provider lookup,\ntransport, timeout, body-disclosure, and protocol errors. Defaults to\nBlock. Allow and Ignore skip the failed callout's mutations and continue.\nA valid immediate response from the provider is always honored.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private FailStrategy failStrategy = io.fabric8.kubernetes.client.utils.Serialization.unmarshal("\"Block\"", FailStrategy.class);

    public FailStrategy getFailStrategy() {
        return failStrategy;
    }

    public void setFailStrategy(FailStrategy failStrategy) {
        this.failStrategy = failStrategy;
    }

    /**
     * Provider identifies the HTTP callout provider. The provider owns the
     * URL, timeout, TLS, and decision-response size limit. A missing, unavailable,
     * or wrong-type provider is an execution failure handled by FailStrategy;
     * no other provider is used as a fallback.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("provider")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Provider identifies the HTTP callout provider. The provider owns the\nURL, timeout, TLS, and decision-response size limit. A missing, unavailable,\nor wrong-type provider is an execution failure handled by FailStrategy;\nno other provider is used as a fallback.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.httpcallout.Provider provider;

    public io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.httpcallout.Provider getProvider() {
        return provider;
    }

    public void setProvider(io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.httpcallout.Provider provider) {
        this.provider = provider;
    }

    /**
     * Request enables a callout before the request is forwarded upstream.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("request")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Request enables a callout before the request is forwarded upstream.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.httpcallout.Request request;

    public io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.httpcallout.Request getRequest() {
        return request;
    }

    public void setRequest(io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.httpcallout.Request request) {
        this.request = request;
    }

    /**
     * Response enables a callout on the upstream response. Its invocation
     * includes request correlation metadata, but no request headers or body.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("response")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Response enables a callout on the upstream response. Its invocation\nincludes request correlation metadata, but no request headers or body.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.httpcallout.Response response;

    public io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.httpcallout.Response getResponse() {
        return response;
    }

    public void setResponse(io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.httpcallout.Response response) {
        this.response = response;
    }
}

