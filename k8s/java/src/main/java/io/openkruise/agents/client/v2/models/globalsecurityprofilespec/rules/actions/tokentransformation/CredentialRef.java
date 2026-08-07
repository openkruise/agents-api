package io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"credentialProvider","kind","name","namespace","secret"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class CredentialRef implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * CredentialProvider fetches credentials from an external provider.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("credentialProvider")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("CredentialProvider fetches credentials from an external provider.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.credentialref.CredentialProvider credentialProvider;

    public io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.credentialref.CredentialProvider getCredentialProvider() {
        return credentialProvider;
    }

    public void setCredentialProvider(io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.credentialref.CredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
    }

    public enum Kind {

        @com.fasterxml.jackson.annotation.JsonProperty("Secret")
        SECRET("Secret"), @com.fasterxml.jackson.annotation.JsonProperty("CredentialProvider")
        CREDENTIALPROVIDER("CredentialProvider");

        java.lang.String value;

        Kind(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * Kind identifies the deprecated credential source type.
     * Deprecated: use Secret or CredentialProvider.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("kind")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Kind identifies the deprecated credential source type.\nDeprecated: use Secret or CredentialProvider.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Kind kind;

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    /**
     * Name identifies the deprecated credential source.
     * Deprecated: use Secret or CredentialProvider.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Name identifies the deprecated credential source.\nDeprecated: use Secret or CredentialProvider.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Namespace is used by deprecated Secret references. It is ignored by
     * deprecated CredentialProvider references.
     * Deprecated: use Secret.Namespace instead.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("namespace")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Namespace is used by deprecated Secret references. It is ignored by\ndeprecated CredentialProvider references.\nDeprecated: use Secret.Namespace instead.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String namespace;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Secret references credentials stored in a Kubernetes Secret.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("secret")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Secret references credentials stored in a Kubernetes Secret.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.credentialref.Secret secret;

    public io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.credentialref.Secret getSecret() {
        return secret;
    }

    public void setSecret(io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.credentialref.Secret secret) {
        this.secret = secret;
    }
}

