package io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2.client;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"secretKeyRef"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class ClientSecret implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * SecretKeyRef names the Secret and key holding the value.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("secretKeyRef")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("SecretKeyRef names the Secret and key holding the value.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2.client.clientsecret.SecretKeyRef secretKeyRef;

    public io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2.client.clientsecret.SecretKeyRef getSecretKeyRef() {
        return secretKeyRef;
    }

    public void setSecretKeyRef(io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2.client.clientsecret.SecretKeyRef secretKeyRef) {
        this.secretKeyRef = secretKeyRef;
    }
}

