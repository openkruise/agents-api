package io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"body","statusCode"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Block implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Body is an optional response body sent verbatim to the client.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("body")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Body is an optional response body sent verbatim to the client.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    /**
     * StatusCode is the HTTP status returned to the client.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("statusCode")
    @io.fabric8.generator.annotation.Max(599.0)
    @io.fabric8.generator.annotation.Min(100.0)
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("StatusCode is the HTTP status returned to the client.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer statusCode = 403;

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
}

