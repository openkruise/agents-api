package io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.audit.webhook;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"body","headers","method"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Request implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Body is the request body. Exactly one of Body.JSON or Body.Text must
     * be set. Omitting Body sends an empty request.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("body")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Body is the request body. Exactly one of Body.JSON or Body.Text must\nbe set. Omitting Body sends an empty request.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.audit.webhook.request.Body body;

    public io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.audit.webhook.request.Body getBody() {
        return body;
    }

    public void setBody(io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.audit.webhook.request.Body body) {
        this.body = body;
    }

    /**
     * Headers are appended to the request after the default Content-Type
     * header.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("headers")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Headers are appended to the request after the default Content-Type\nheader.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.audit.webhook.request.Headers> headers;

    public java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.audit.webhook.request.Headers> getHeaders() {
        return headers;
    }

    public void setHeaders(java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.audit.webhook.request.Headers> headers) {
        this.headers = headers;
    }

    public enum Method {

        @com.fasterxml.jackson.annotation.JsonProperty("POST")
        POST("POST"), @com.fasterxml.jackson.annotation.JsonProperty("PUT")
        PUT("PUT"), @com.fasterxml.jackson.annotation.JsonProperty("PATCH")
        PATCH("PATCH");

        java.lang.String value;

        Method(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * Method is the HTTP request method. Defaults to POST.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("method")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Method is the HTTP request method. Defaults to POST.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Method method = io.fabric8.kubernetes.client.utils.Serialization.unmarshal("\"POST\"", Method.class);

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}

