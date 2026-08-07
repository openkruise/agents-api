package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"audit","inputs","priority","rules","selector"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class SecurityProfileSpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Audit defines the audit actions inherited by rules that do not configure
     * their own Audit list.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("audit")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Audit defines the audit actions inherited by rules that do not configure\ntheir own Audit list.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.Audit> audit;

    public java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.Audit> getAudit() {
        return audit;
    }

    public void setAudit(java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.Audit> audit) {
        this.audit = audit;
    }

    /**
     * Inputs defines named values available to CEL expressions and Go
     * templates in this profile.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("inputs")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Inputs defines named values available to CEL expressions and Go\ntemplates in this profile.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.Inputs> inputs;

    public java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.Inputs> getInputs() {
        return inputs;
    }

    public void setInputs(java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.Inputs> inputs) {
        this.inputs = inputs;
    }

    /**
     * Priority determines evaluation order when multiple profiles match a Pod.
     * Lower values run first. Ties are resolved by creation time, name, and
     * namespace. Defaults to DefaultSecurityProfilePriority.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("priority")
    @io.fabric8.generator.annotation.Min(0.0)
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Priority determines evaluation order when multiple profiles match a Pod.\nLower values run first. Ties are resolved by creation time, name, and\nnamespace. Defaults to DefaultSecurityProfilePriority.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer priority = 1000;

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * Rules is the ordered rule chain. Every matching rule executes until an
     * action terminates the request. Rules from matching profiles are combined
     * in profile evaluation order. An empty list forwards all traffic.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("rules")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Rules is the ordered rule chain. Every matching rule executes until an\naction terminates the request. Rules from matching profiles are combined\nin profile evaluation order. An empty list forwards all traffic.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.Rules> rules;

    public java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.Rules> getRules() {
        return rules;
    }

    public void setRules(java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.Rules> rules) {
        this.rules = rules;
    }

    /**
     * Selector chooses the Pods to which this profile applies. An empty
     * selector matches every Pod in scope.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("selector")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Selector chooses the Pods to which this profile applies. An empty\nselector matches every Pod in scope.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.Selector selector;

    public io.openkruise.agents.client.v2.models.securityprofilespec.Selector getSelector() {
        return selector;
    }

    public void setSelector(io.openkruise.agents.client.v2.models.securityprofilespec.Selector selector) {
        this.selector = selector;
    }
}

