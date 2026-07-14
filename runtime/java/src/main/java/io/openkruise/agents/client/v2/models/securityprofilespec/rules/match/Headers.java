package io.openkruise.agents.client.v2.models.securityprofilespec.rules.match;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"name","type","value"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Headers implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Name is the header name (case-insensitive). Restricted to a safe
     * subset of RFC 7230 tchar characters.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    @io.fabric8.generator.annotation.Required()
    @io.fabric8.generator.annotation.Pattern("^[A-Za-z0-9!#$%&'*+\\-.^_|~]+$")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Name is the header name (case-insensitive). Restricted to a safe\nsubset of RFC 7230 tchar characters.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public enum Type {

        @com.fasterxml.jackson.annotation.JsonProperty("Exact")
        EXACT("Exact"), @com.fasterxml.jackson.annotation.JsonProperty("Prefix")
        PREFIX("Prefix"), @com.fasterxml.jackson.annotation.JsonProperty("Regex")
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
     * Type selects the matching strategy. Defaults to Exact.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("type")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Type selects the matching strategy. Defaults to Exact.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Type type = io.fabric8.kubernetes.client.utils.Serialization.unmarshal("\"Exact\"", Type.class);

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Value is the match operand. For Exact/Prefix it is compared
     * verbatim; for Regex it is interpreted as an RE2 expression.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("value")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Value is the match operand. For Exact/Prefix it is compared\nverbatim; for Regex it is interpreted as an RE2 expression.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

