package io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.credentialref.credentialprovider;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"cel","template","value"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Parameters implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Cel is a CEL expression evaluated against the request, Pod, profile,
     * rule, and inputs context.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("cel")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Cel is a CEL expression evaluated against the request, Pod, profile,\nrule, and inputs context.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String cel;

    public String getCel() {
        return cel;
    }

    public void setCel(String cel) {
        this.cel = cel;
    }

    /**
     * Template is a Go template whose rendered output is the value.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("template")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Template is a Go template whose rendered output is the value.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String template;

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * Value is a static string emitted verbatim.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("value")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Value is a static string emitted verbatim.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

