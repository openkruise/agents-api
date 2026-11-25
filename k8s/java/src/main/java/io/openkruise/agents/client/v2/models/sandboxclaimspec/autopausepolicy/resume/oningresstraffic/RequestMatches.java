package io.openkruise.agents.client.v2.models.sandboxclaimspec.autopausepolicy.resume.oningresstraffic;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"headers"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class RequestMatches implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Headers lists header conditions that must all match.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("headers")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Headers lists header conditions that must all match.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.sandboxclaimspec.autopausepolicy.resume.oningresstraffic.requestmatches.Headers> headers;

    public java.util.List<io.openkruise.agents.client.v2.models.sandboxclaimspec.autopausepolicy.resume.oningresstraffic.requestmatches.Headers> getHeaders() {
        return headers;
    }

    public void setHeaders(java.util.List<io.openkruise.agents.client.v2.models.sandboxclaimspec.autopausepolicy.resume.oningresstraffic.requestmatches.Headers> headers) {
        this.headers = headers;
    }
}

