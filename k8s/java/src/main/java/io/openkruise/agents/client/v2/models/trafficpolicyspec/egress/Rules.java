package io.openkruise.agents.client.v2.models.trafficpolicyspec.egress;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"action","from","ports","to"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Rules implements io.fabric8.kubernetes.api.model.KubernetesResource {

    public enum Action {

        @com.fasterxml.jackson.annotation.JsonProperty("allow")
        ALLOW("allow"), @com.fasterxml.jackson.annotation.JsonProperty("reject")
        REJECT("reject");

        java.lang.String value;

        Action(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * Action determines whether matched traffic is allowed or denied.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("action")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Action determines whether matched traffic is allowed or denied.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Action action;

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * From lists source peers. Multiple entries are ORed.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("from")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("From lists source peers. Multiple entries are ORed.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.trafficpolicyspec.egress.rules.From> from;

    public java.util.List<io.openkruise.agents.client.v2.models.trafficpolicyspec.egress.rules.From> getFrom() {
        return from;
    }

    public void setFrom(java.util.List<io.openkruise.agents.client.v2.models.trafficpolicyspec.egress.rules.From> from) {
        this.from = from;
    }

    /**
     * Ports restricts this rule to specific L4 protocol/port combinations.
     * Multiple entries are ORed. If empty, the rule matches all ports.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("ports")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Ports restricts this rule to specific L4 protocol/port combinations.\nMultiple entries are ORed. If empty, the rule matches all ports.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.trafficpolicyspec.egress.rules.Ports> ports;

    public java.util.List<io.openkruise.agents.client.v2.models.trafficpolicyspec.egress.rules.Ports> getPorts() {
        return ports;
    }

    public void setPorts(java.util.List<io.openkruise.agents.client.v2.models.trafficpolicyspec.egress.rules.Ports> ports) {
        this.ports = ports;
    }

    /**
     * To lists destination peers. Multiple entries are ORed.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("to")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("To lists destination peers. Multiple entries are ORed.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.trafficpolicyspec.egress.rules.To> to;

    public java.util.List<io.openkruise.agents.client.v2.models.trafficpolicyspec.egress.rules.To> getTo() {
        return to;
    }

    public void setTo(java.util.List<io.openkruise.agents.client.v2.models.trafficpolicyspec.egress.rules.To> to) {
        this.to = to;
    }
}

