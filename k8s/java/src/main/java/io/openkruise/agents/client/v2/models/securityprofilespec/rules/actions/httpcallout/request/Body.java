package io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.httpcallout.request;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"enabled","maxBytes"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Body implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Enabled buffers and sends the complete body when true. The callout then
     * runs after buffering, instead of at the headers phase. Defaults to false.
     * An empty body is valid. Bodies must be valid UTF-8 and fit within
     * MaxBytes; they are never truncated. In the response phase, only the
     * response body is sent.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("enabled")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Enabled buffers and sends the complete body when true. The callout then\nruns after buffering, instead of at the headers phase. Defaults to false.\nAn empty body is valid. Bodies must be valid UTF-8 and fit within\nMaxBytes; they are never truncated. In the response phase, only the\nresponse body is sent.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Boolean enabled = false;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * MaxBytes limits the body disclosed in this phase. Defaults to 1 MiB and
     * may not exceed 8 MiB. Oversized content is an execution failure, not
     * truncated input. This has no effect when Enabled is false and does not
     * configure Envoy's buffering limit or the provider's decision-response
     * size limit.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("maxBytes")
    @io.fabric8.generator.annotation.Max(8388608.0)
    @io.fabric8.generator.annotation.Min(1.0)
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("MaxBytes limits the body disclosed in this phase. Defaults to 1 MiB and\nmay not exceed 8 MiB. Oversized content is an execution failure, not\ntruncated input. This has no effect when Enabled is false and does not\nconfigure Envoy's buffering limit or the provider's decision-response\nsize limit.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Long maxBytes = 1048576L;

    public Long getMaxBytes() {
        return maxBytes;
    }

    public void setMaxBytes(Long maxBytes) {
        this.maxBytes = maxBytes;
    }
}

