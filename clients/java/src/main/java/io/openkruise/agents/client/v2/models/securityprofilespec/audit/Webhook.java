package io.openkruise.agents.client.v2.models.securityprofilespec.audit;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"request","timeout","url"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Webhook implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Request describes the HTTP request shape. Defaults to method=POST
     * and empty body when omitted.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("request")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Request describes the HTTP request shape. Defaults to method=POST\nand empty body when omitted.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.audit.webhook.Request request;

    public io.openkruise.agents.client.v2.models.securityprofilespec.audit.webhook.Request getRequest() {
        return request;
    }

    public void setRequest(io.openkruise.agents.client.v2.models.securityprofilespec.audit.webhook.Request request) {
        this.request = request;
    }

    /**
     * Timeout caps each HTTP attempt. Defaults to 2s, max 30s.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("timeout")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Timeout caps each HTTP attempt. Defaults to 2s, max 30s.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String timeout = "2s";

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    /**
     * URL is the absolute HTTP(S) URL of the webhook. Supports Go
     * text/template expressions over AuditContext, allowing per-Pod
     * addressing such as: http://{{ .Pod.IP }}:8080/audit
     *
     * Rendering failures (template error or non-HTTP scheme) cause the
     * event to be dropped and counted under
     * traffix_extension_audit_webhook_dropped_total{reason="render_url"}.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("url")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("URL is the absolute HTTP(S) URL of the webhook. Supports Go\ntext/template expressions over AuditContext, allowing per-Pod\naddressing such as: http://{{ .Pod.IP }}:8080/audit\n\nRendering failures (template error or non-HTTP scheme) cause the\nevent to be dropped and counted under\ntraffix_extension_audit_webhook_dropped_total{reason=\"render_url\"}.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

