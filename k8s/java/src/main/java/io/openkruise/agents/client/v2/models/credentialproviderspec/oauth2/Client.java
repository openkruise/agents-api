package io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"clientId","clientSecret"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Client implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * ClientID of the registered application. Not a secret.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("clientId")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ClientID of the registered application. Not a secret.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * ClientSecret references the Secret holding the client secret. It is a
     * reference rather than a value so the secret never appears in this object,
     * which is readable by anything that can read the CRD.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("clientSecret")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ClientSecret references the Secret holding the client secret. It is a\nreference rather than a value so the secret never appears in this object,\nwhich is readable by anything that can read the CRD.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2.client.ClientSecret clientSecret;

    public io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2.client.ClientSecret getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2.client.ClientSecret clientSecret) {
        this.clientSecret = clientSecret;
    }
}

