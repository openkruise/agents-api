package io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.credentialref;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"name","namespace","parameters"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class CredentialProvider implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Name is the provider name.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Name is the provider name.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Namespace is reserved for namespace-scoped provider lookup. It is
     * currently ignored.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("namespace")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Namespace is reserved for namespace-scoped provider lookup. It is\ncurrently ignored.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String namespace;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Parameters supplies values rendered into the provider request's
     * extraMetadata field.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("parameters")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Parameters supplies values rendered into the provider request's\nextraMetadata field.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.Map<java.lang.String, io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.credentialref.credentialprovider.Parameters> parameters;

    public java.util.Map<java.lang.String, io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.credentialref.credentialprovider.Parameters> getParameters() {
        return parameters;
    }

    public void setParameters(java.util.Map<java.lang.String, io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.credentialref.credentialprovider.Parameters> parameters) {
        this.parameters = parameters;
    }
}

