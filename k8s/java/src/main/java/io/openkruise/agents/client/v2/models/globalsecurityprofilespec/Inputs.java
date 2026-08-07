package io.openkruise.agents.client.v2.models.globalsecurityprofilespec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"configMap","inline","name"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Inputs implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * ConfigMap sources the input values from a ConfigMap's data.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("configMap")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ConfigMap sources the input values from a ConfigMap's data.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.globalsecurityprofilespec.inputs.ConfigMap configMap;

    public io.openkruise.agents.client.v2.models.globalsecurityprofilespec.inputs.ConfigMap getConfigMap() {
        return configMap;
    }

    public void setConfigMap(io.openkruise.agents.client.v2.models.globalsecurityprofilespec.inputs.ConfigMap configMap) {
        this.configMap = configMap;
    }

    /**
     * Inline declares the input values directly in the profile.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("inline")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Inline declares the input values directly in the profile.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.Map<java.lang.String, String> inline;

    public java.util.Map<java.lang.String, String> getInline() {
        return inline;
    }

    public void setInline(java.util.Map<java.lang.String, String> inline) {
        this.inline = inline;
    }

    /**
     * Name uniquely identifies this input within the profile.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    @io.fabric8.generator.annotation.Required()
    @io.fabric8.generator.annotation.Pattern("^[a-z0-9]([-a-z0-9]*[a-z0-9])?$")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Name uniquely identifies this input within the profile.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

