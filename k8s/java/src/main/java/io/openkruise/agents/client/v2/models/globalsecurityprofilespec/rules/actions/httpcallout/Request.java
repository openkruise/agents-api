package io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.httpcallout;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"body","headers"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Request implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Body controls body disclosure. Omission disables body disclosure.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("body")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Body controls body disclosure. Omission disables body disclosure.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.httpcallout.request.Body body;

    public io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.httpcallout.request.Body getBody() {
        return body;
    }

    public void setBody(io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.httpcallout.request.Body body) {
        this.body = body;
    }

    /**
     * Headers controls header disclosure. Omission disables header disclosure.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("headers")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Headers controls header disclosure. Omission disables header disclosure.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.httpcallout.request.Headers headers;

    public io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.httpcallout.request.Headers getHeaders() {
        return headers;
    }

    public void setHeaders(io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.httpcallout.request.Headers headers) {
        this.headers = headers;
    }
}

