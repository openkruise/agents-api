package io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"kind","name"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class CredentialRef implements io.fabric8.kubernetes.api.model.KubernetesResource {

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
     * Kind selects the credential source type.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("kind")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Kind selects the credential source type.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Kind kind;

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    /**
     * Name is the resource name — Secret name for Kind=Secret, or
     * provider name for Kind=CredentialProvider.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Name is the resource name — Secret name for Kind=Secret, or\nprovider name for Kind=CredentialProvider.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

