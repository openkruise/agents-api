package io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.httpcallout.response;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"enabled","filter"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Headers implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Enabled sends headers when true. Defaults to false. When false, Filter
     * has no effect. Request metadata such as authority, path, and content
     * type remains part of the invocation regardless of this setting.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("enabled")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Enabled sends headers when true. Defaults to false. When false, Filter\nhas no effect. Request metadata such as authority, path, and content\ntype remains part of the invocation regardless of this setting.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Boolean enabled = false;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Filter selects the headers sent when Enabled is true. Omission sends
     * every header, including credentials and cookies. When present, exactly
     * one non-empty list must be set; an empty filter is invalid.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("filter")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Filter selects the headers sent when Enabled is true. Omission sends\nevery header, including credentials and cookies. When present, exactly\none non-empty list must be set; an empty filter is invalid.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.httpcallout.response.headers.Filter filter;

    public io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.httpcallout.response.headers.Filter getFilter() {
        return filter;
    }

    public void setFilter(io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.httpcallout.response.headers.Filter filter) {
        this.filter = filter;
    }
}

