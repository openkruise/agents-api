package io.openkruise.agents.client.v2.models.trafficpolicyspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"rules"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Ingress implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Rules is the ordered rule list for this direction.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("rules")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Rules is the ordered rule list for this direction.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.trafficpolicyspec.ingress.Rules> rules;

    public java.util.List<io.openkruise.agents.client.v2.models.trafficpolicyspec.ingress.Rules> getRules() {
        return rules;
    }

    public void setRules(java.util.List<io.openkruise.agents.client.v2.models.trafficpolicyspec.ingress.Rules> rules) {
        this.rules = rules;
    }
}

