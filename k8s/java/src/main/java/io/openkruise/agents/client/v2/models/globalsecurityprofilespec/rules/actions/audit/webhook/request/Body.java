package io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.audit.webhook.request;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"json","text"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Body implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * JSON is a structured body. String values are rendered as Go templates;
     * other values are preserved. The content type is application/json.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("json")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("JSON is a structured body. String values are rendered as Go templates;\nother values are preserved. The content type is application/json.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.audit.webhook.request.body.Json json;

    public io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.audit.webhook.request.body.Json getJson() {
        return json;
    }

    public void setJson(io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.audit.webhook.request.body.Json json) {
        this.json = json;
    }

    /**
     * Text is a Go template rendered as a text/plain body.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("text")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Text is a Go template rendered as a text/plain body.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

