package io.openkruise.agents.client.v2.models.commitspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"credentials","secrets"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class RegistryAuth implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Credentials is reserved for future use. Currently has no effect.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("credentials")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Credentials is reserved for future use. Currently has no effect.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.Map<java.lang.String, String> credentials;

    public java.util.Map<java.lang.String, String> getCredentials() {
        return credentials;
    }

    public void setCredentials(java.util.Map<java.lang.String, String> credentials) {
        this.credentials = credentials;
    }

    /**
     * Secrets is a list of dockerconfigjson Secret names in the same namespace.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("secrets")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Secrets is a list of dockerconfigjson Secret names in the same namespace.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<String> secrets;

    public java.util.List<String> getSecrets() {
        return secrets;
    }

    public void setSecrets(java.util.List<String> secrets) {
        this.secrets = secrets;
    }
}

