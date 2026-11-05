package io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.httpcallout.request.headers;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"allowList","denyList"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Filter implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * AllowList names the only headers sent. Header names are case-insensitive.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("allowList")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("AllowList names the only headers sent. Header names are case-insensitive.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<String> allowList;

    public java.util.List<String> getAllowList() {
        return allowList;
    }

    public void setAllowList(java.util.List<String> allowList) {
        this.allowList = allowList;
    }

    /**
     * DenyList names the headers omitted. Header names are case-insensitive.
     * Credentials are sent unless explicitly excluded.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("denyList")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("DenyList names the headers omitted. Header names are case-insensitive.\nCredentials are sent unless explicitly excluded.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<String> denyList;

    public java.util.List<String> getDenyList() {
        return denyList;
    }

    public void setDenyList(java.util.List<String> denyList) {
        this.denyList = denyList;
    }
}

