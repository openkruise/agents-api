package io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"remove","set"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class HeaderManipulation implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Remove lists header names to strip from the request before it is
     * forwarded upstream. Names must be lowercase; header names are
     * case-insensitive on the wire. A name may not appear in both Set and
     * Remove.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("remove")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Remove lists header names to strip from the request before it is\nforwarded upstream. Names must be lowercase; header names are\ncase-insensitive on the wire. A name may not appear in both Set and\nRemove.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<String> remove;

    public java.util.List<String> getRemove() {
        return remove;
    }

    public void setRemove(java.util.List<String> remove) {
        this.remove = remove;
    }

    /**
     * Set adds or replaces headers. An existing header with the same
     * name is replaced.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("set")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Set adds or replaces headers. An existing header with the same\nname is replaced.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.headermanipulation.Set> set;

    public java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.headermanipulation.Set> getSet() {
        return set;
    }

    public void setSet(java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.headermanipulation.Set> set) {
        this.set = set;
    }
}

