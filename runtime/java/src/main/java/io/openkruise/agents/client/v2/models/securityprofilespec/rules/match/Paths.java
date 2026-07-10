package io.openkruise.agents.client.v2.models.securityprofilespec.rules.match;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"type","value"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Paths implements io.fabric8.kubernetes.api.model.KubernetesResource {

    public enum Type {

        @com.fasterxml.jackson.annotation.JsonProperty("Prefix")
        PREFIX("Prefix"), @com.fasterxml.jackson.annotation.JsonProperty("Exact")
        EXACT("Exact"), @com.fasterxml.jackson.annotation.JsonProperty("Regex")
        REGEX("Regex");

        java.lang.String value;

        Type(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * PathMatchType enumerates URL path matching strategies.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("type")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("PathMatchType enumerates URL path matching strategies.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Type type = io.fabric8.kubernetes.client.utils.Serialization.unmarshal("\"Prefix\"", Type.class);

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Value is the match pattern. For Regex, it is an RE2 expression and
     * must be <= 256 characters.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("value")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Value is the match pattern. For Regex, it is an RE2 expression and\nmust be <= 256 characters.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

