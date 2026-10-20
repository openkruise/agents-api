package io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.oauth2.operation;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"allowedGrantTypes"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Token implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * AllowedGrantTypes optionally restricts requests to exact grant_type values,
     * including extension grant URIs. Omitted or empty adds no policy restriction;
     * protocol support and service authorization still apply.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("allowedGrantTypes")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("AllowedGrantTypes optionally restricts requests to exact grant_type values,\nincluding extension grant URIs. Omitted or empty adds no policy restriction;\nprotocol support and service authorization still apply.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<String> allowedGrantTypes;

    public java.util.List<String> getAllowedGrantTypes() {
        return allowedGrantTypes;
    }

    public void setAllowedGrantTypes(java.util.List<String> allowedGrantTypes) {
        this.allowedGrantTypes = allowedGrantTypes;
    }
}

