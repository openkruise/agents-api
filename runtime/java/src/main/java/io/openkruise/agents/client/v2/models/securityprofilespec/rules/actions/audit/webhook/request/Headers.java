package io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.audit.webhook.request;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"name","value"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Headers implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Name is the header name. Restricted to a safe subset of RFC 7230
     * tchar characters.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    @io.fabric8.generator.annotation.Required()
    @io.fabric8.generator.annotation.Pattern("^[A-Za-z0-9!#$%&'*+\\-.^_|~]+$")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Name is the header name. Restricted to a safe subset of RFC 7230\ntchar characters.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Value is the header value template.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("value")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Value is the header value template.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

