package io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.httpcallout;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"name"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Provider implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Name identifies the provider.
     * It is required and has no default.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    @io.fabric8.generator.annotation.Required()
    @io.fabric8.generator.annotation.Pattern("^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Name identifies the provider.\nIt is required and has no default.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

