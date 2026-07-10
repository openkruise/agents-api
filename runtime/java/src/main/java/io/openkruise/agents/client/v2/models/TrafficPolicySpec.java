package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"egress","ingress","priority","selector"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class TrafficPolicySpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Egress defines rules applied to outbound traffic of selected pods.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("egress")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Egress defines rules applied to outbound traffic of selected pods.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.trafficpolicyspec.Egress egress;

    public io.openkruise.agents.client.v2.models.trafficpolicyspec.Egress getEgress() {
        return egress;
    }

    public void setEgress(io.openkruise.agents.client.v2.models.trafficpolicyspec.Egress egress) {
        this.egress = egress;
    }

    /**
     * Ingress defines rules applied to inbound traffic of selected pods.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("ingress")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Ingress defines rules applied to inbound traffic of selected pods.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.trafficpolicyspec.Ingress ingress;

    public io.openkruise.agents.client.v2.models.trafficpolicyspec.Ingress getIngress() {
        return ingress;
    }

    public void setIngress(io.openkruise.agents.client.v2.models.trafficpolicyspec.Ingress ingress) {
        this.ingress = ingress;
    }

    /**
     * Priority determines the evaluation order when multiple TrafficPolicies
     * match the same pod. Higher values are evaluated first. When two
     * policies share the same priority, the result is implementation-defined.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("priority")
    @io.fabric8.generator.annotation.Min(0.0)
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Priority determines the evaluation order when multiple TrafficPolicies\nmatch the same pod. Higher values are evaluated first. When two\npolicies share the same priority, the result is implementation-defined.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer priority = 1000;

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * Selector chooses the pods this policy applies to. Standard
     * LabelSelector semantics: an EMPTY selector matches EVERY pod within
     * the policy's scope (namespace for TrafficPolicy, cluster-wide for
     * GlobalTrafficPolicy).
     */
    @com.fasterxml.jackson.annotation.JsonProperty("selector")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Selector chooses the pods this policy applies to. Standard\nLabelSelector semantics: an EMPTY selector matches EVERY pod within\nthe policy's scope (namespace for TrafficPolicy, cluster-wide for\nGlobalTrafficPolicy).")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.trafficpolicyspec.Selector selector;

    public io.openkruise.agents.client.v2.models.trafficpolicyspec.Selector getSelector() {
        return selector;
    }

    public void setSelector(io.openkruise.agents.client.v2.models.trafficpolicyspec.Selector selector) {
        this.selector = selector;
    }
}

