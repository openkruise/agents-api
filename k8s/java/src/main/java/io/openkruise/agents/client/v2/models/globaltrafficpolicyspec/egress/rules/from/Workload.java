package io.openkruise.agents.client.v2.models.globaltrafficpolicyspec.egress.rules.from;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"namespace","selector"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Workload implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Namespace of the target pods.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("namespace")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Namespace of the target pods.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String namespace;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Selector is a label selector that matches the target pods.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("selector")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Selector is a label selector that matches the target pods.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.Map<java.lang.String, String> selector;

    public java.util.Map<java.lang.String, String> getSelector() {
        return selector;
    }

    public void setSelector(java.util.Map<java.lang.String, String> selector) {
        this.selector = selector;
    }
}

