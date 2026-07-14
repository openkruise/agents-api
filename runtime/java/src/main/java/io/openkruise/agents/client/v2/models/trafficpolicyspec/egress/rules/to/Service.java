package io.openkruise.agents.client.v2.models.trafficpolicyspec.egress.rules.to;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"name","namespace"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Service implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Name is the Service resource name.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Name is the Service resource name.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Namespace is the namespace of the target Service. When omitted, defaults
     * to the namespace of the enclosing TrafficPolicy.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("namespace")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Namespace is the namespace of the target Service. When omitted, defaults\nto the namespace of the enclosing TrafficPolicy.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String namespace;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}

