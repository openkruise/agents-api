package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"audit","rules","selector"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class SecurityProfileSpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Audit declares profile-wide audit entries. They fire for every
     * matched rule (subject to each entry's `When` CEL expression),
     * providing a default audit configuration for all rules in this
     * profile. A SecurityRule may override this list via
     * SecurityRuleActions.Audit.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("audit")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Audit declares profile-wide audit entries. They fire for every\nmatched rule (subject to each entry's `When` CEL expression),\nproviding a default audit configuration for all rules in this\nprofile. A SecurityRule may override this list via\nSecurityRuleActions.Audit.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.Audit> audit;

    public java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.Audit> getAudit() {
        return audit;
    }

    public void setAudit(java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.Audit> audit) {
        this.audit = audit;
    }

    /**
     * Rules is the ordered rule chain. Semantics are Default Continue:
     * all matching rules' actions run in order until a terminal action
     * (Block / Bypass) short-circuits the chain. An empty rule chain is
     * equivalent to "forward everything to the original destination".
     */
    @com.fasterxml.jackson.annotation.JsonProperty("rules")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Rules is the ordered rule chain. Semantics are Default Continue:\nall matching rules' actions run in order until a terminal action\n(Block / Bypass) short-circuits the chain. An empty rule chain is\nequivalent to \"forward everything to the original destination\".")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.Rules> rules;

    public java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.Rules> getRules() {
        return rules;
    }

    public void setRules(java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.Rules> rules) {
        this.rules = rules;
    }

    /**
     * Selector chooses the Pods this profile applies to. Standard
     * LabelSelector semantics: an EMPTY selector (no matchLabels and no
     * matchExpressions) matches EVERY pod in the same namespace, in line
     * with NetworkPolicy / Istio AuthorizationPolicy. Use a deliberate
     * matchExpression (e.g. `key: __none__, operator: DoesNotExist`) to
     * express "match nothing".
     */
    @com.fasterxml.jackson.annotation.JsonProperty("selector")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Selector chooses the Pods this profile applies to. Standard\nLabelSelector semantics: an EMPTY selector (no matchLabels and no\nmatchExpressions) matches EVERY pod in the same namespace, in line\nwith NetworkPolicy / Istio AuthorizationPolicy. Use a deliberate\nmatchExpression (e.g. `key: __none__, operator: DoesNotExist`) to\nexpress \"match nothing\".")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.Selector selector;

    public io.openkruise.agents.client.v2.models.securityprofilespec.Selector getSelector() {
        return selector;
    }

    public void setSelector(io.openkruise.agents.client.v2.models.securityprofilespec.Selector selector) {
        this.selector = selector;
    }
}

