package io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.apikey;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"cel","names"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class TargetHeaders implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Cel selects request header names dynamically and must return a
     * list<string>. The expression may read the request, Pod, profile, rule,
     * and inputs context, but not the retrieved token.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("cel")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Cel selects request header names dynamically and must return a\nlist<string>. The expression may read the request, Pod, profile, rule,\nand inputs context, but not the retrieved token.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String cel;

    public String getCel() {
        return cel;
    }

    public void setCel(String cel) {
        this.cel = cel;
    }

    /**
     * Names is a static set of request header names. Every selected header uses
     * the same value rule.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("names")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Names is a static set of request header names. Every selected header uses\nthe same value rule.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<String> names;

    public java.util.List<String> getNames() {
        return names;
    }

    public void setNames(java.util.List<String> names) {
        this.names = names;
    }
}

