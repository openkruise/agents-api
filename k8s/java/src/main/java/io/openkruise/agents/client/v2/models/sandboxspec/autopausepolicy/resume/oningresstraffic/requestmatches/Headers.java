package io.openkruise.agents.client.v2.models.sandboxspec.autopausepolicy.resume.oningresstraffic.requestmatches;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"name","operator","values"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Headers implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Name is the header name, matched case-insensitively. Pseudo-headers
     * are not supported.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    @io.fabric8.generator.annotation.Required()
    @io.fabric8.generator.annotation.Pattern("^[A-Za-z0-9!#$%&'*+\\-.^_|~]+$")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Name is the header name, matched case-insensitively. Pseudo-headers\nare not supported.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public enum Operator {

        @com.fasterxml.jackson.annotation.JsonProperty("In")
        IN("In"), @com.fasterxml.jackson.annotation.JsonProperty("NotIn")
        NOTIN("NotIn");

        java.lang.String value;

        Operator(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * Operator selects the matching operation and defaults to In when omitted.
     * In requires at least one header value to be in Values; an absent header
     * does not match. NotIn requires every header value to be outside Values;
     * an absent header matches. Unknown operators do not match.
     * This condition applies only to its RequestMatches entry and does not
     * veto other OR entries.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("operator")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Operator selects the matching operation and defaults to In when omitted.\nIn requires at least one header value to be in Values; an absent header\ndoes not match. NotIn requires every header value to be outside Values;\nan absent header matches. Unknown operators do not match.\nThis condition applies only to its RequestMatches entry and does not\nveto other OR entries.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Operator operator = io.fabric8.kubernetes.client.utils.Serialization.unmarshal("\"In\"", Operator.class);

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    /**
     * Values is the set of exact, case-sensitive header values to compare against.
     * In and NotIn both require at least one value; all values of a repeated
     * header are checked.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("values")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Values is the set of exact, case-sensitive header values to compare against.\nIn and NotIn both require at least one value; all values of a repeated\nheader are checked.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<String> values;

    public java.util.List<String> getValues() {
        return values;
    }

    public void setValues(java.util.List<String> values) {
        this.values = values;
    }
}

