package io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.audit;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"request","timeout","url"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Webhook implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Request configures the HTTP request. When omitted, a POST request with an
     * empty body is sent.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("request")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Request configures the HTTP request. When omitted, a POST request with an\nempty body is sent.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.audit.webhook.Request request;

    public io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.audit.webhook.Request getRequest() {
        return request;
    }

    public void setRequest(io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.audit.webhook.Request request) {
        this.request = request;
    }

    /**
     * Timeout limits each HTTP attempt. Defaults to 2s and must be between
     * 500ms and 30s.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("timeout")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Timeout limits each HTTP attempt. Defaults to 2s and must be between\n500ms and 30s.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String timeout = "2s";

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    /**
     * URL is an absolute HTTP or HTTPS URL rendered as a Go template. The
     * event is dropped if rendering fails or the rendered URL is invalid.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("url")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("URL is an absolute HTTP or HTTPS URL rendered as a Go template. The\nevent is dropped if rendering fails or the rendered URL is invalid.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

