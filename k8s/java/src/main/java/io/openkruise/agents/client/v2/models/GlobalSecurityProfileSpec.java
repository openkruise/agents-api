package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"audit","inputs","priority","rules","selector","tcpRules"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class GlobalSecurityProfileSpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Audit defines the audit actions inherited by rules that do not configure
     * their own Audit list. Applies only to HTTP rules, never TLS rules.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("audit")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Audit defines the audit actions inherited by rules that do not configure\ntheir own Audit list. Applies only to HTTP rules, never TLS rules.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.Audit> audit;

    public java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.Audit> getAudit() {
        return audit;
    }

    public void setAudit(java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.Audit> audit) {
        this.audit = audit;
    }

    /**
     * Inputs defines named values available to CEL expressions and Go
     * templates in this profile.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("inputs")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Inputs defines named values available to CEL expressions and Go\ntemplates in this profile.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.Inputs> inputs;

    public java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.Inputs> getInputs() {
        return inputs;
    }

    public void setInputs(java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.Inputs> inputs) {
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
     * Rules is the ordered rule chain. TLS selection uses the first matching
     * rule; HTTP processing accumulates matching HTTP rules until a terminal
     * action. Rules from matching profiles are combined in profile evaluation
     * order. Requests without a terminal decision continue to TCPRules.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("rules")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Rules is the ordered rule chain. TLS selection uses the first matching\nrule; HTTP processing accumulates matching HTTP rules until a terminal\naction. Rules from matching profiles are combined in profile evaluation\norder. Requests without a terminal decision continue to TCPRules.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.Rules> rules;

    public java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.Rules> getRules() {
        return rules;
    }

    public void setRules(java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.Rules> rules) {
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
    private io.openkruise.agents.client.v2.models.globalsecurityprofilespec.Selector selector;

    public io.openkruise.agents.client.v2.models.globalsecurityprofilespec.Selector getSelector() {
        return selector;
    }

    public void setSelector(io.openkruise.agents.client.v2.models.globalsecurityprofilespec.Selector selector) {
        this.selector = selector;
    }

    /**
     * TCPRules authorizes forwarding destinations at the egress gateway.
     * Applicable profiles contribute rules in the same priority order as Rules.
     * Evaluation proceeds as follows:
     *
     * 1. Evaluate application Rules when the protocol permits. HTTP Bypass or
     *    Block and explicit TLS Passthrough or Deny are terminal and skip TCPRules.
     * 2. Otherwise, evaluate TCPRules in order. The first matching rule's Allow
     *    or Deny ends TCP evaluation across all applicable profiles.
     * 3. If no TCP rule matches, including when the chain is empty, allow.
     *
     * HTTP uses the resolved forwarding IP and port. Unmatched TLS (including
     * missing SNI), raw TCP, and sniffing timeouts use the original destination.
     * TLS termination followed by non-HTTP traffic also uses the original
     * destination. Non-terminal HTTP actions, such as header or token changes,
     * do not skip TCPRules. TrafficPolicy remains independently enforced at the
     * sandbox side, including ingress and UDP; TCPRules does not replace it.
     * For example, in an IPv4 deployment:
     *
     *     tcpRules:
     *     - name: allow-database
     *       match:
     *       - cidrs: ["10.20.0.0/16"]
     *         ports: [5432]
     *       action: Allow
     *     - name: deny-other-ipv4
     *       match:
     *       - cidrs: ["0.0.0.0/0"]
     *       action: Deny
     */
    @com.fasterxml.jackson.annotation.JsonProperty("tcpRules")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TCPRules authorizes forwarding destinations at the egress gateway.\nApplicable profiles contribute rules in the same priority order as Rules.\nEvaluation proceeds as follows:\n\n1. Evaluate application Rules when the protocol permits. HTTP Bypass or\n   Block and explicit TLS Passthrough or Deny are terminal and skip TCPRules.\n2. Otherwise, evaluate TCPRules in order. The first matching rule's Allow\n   or Deny ends TCP evaluation across all applicable profiles.\n3. If no TCP rule matches, including when the chain is empty, allow.\n\nHTTP uses the resolved forwarding IP and port. Unmatched TLS (including\nmissing SNI), raw TCP, and sniffing timeouts use the original destination.\nTLS termination followed by non-HTTP traffic also uses the original\ndestination. Non-terminal HTTP actions, such as header or token changes,\ndo not skip TCPRules. TrafficPolicy remains independently enforced at the\nsandbox side, including ingress and UDP; TCPRules does not replace it.\nFor example, in an IPv4 deployment:\n\n    tcpRules:\n    - name: allow-database\n      match:\n      - cidrs: [\"10.20.0.0/16\"]\n        ports: [5432]\n      action: Allow\n    - name: deny-other-ipv4\n      match:\n      - cidrs: [\"0.0.0.0/0\"]\n      action: Deny")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.TcpRules> tcpRules;

    public java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.TcpRules> getTcpRules() {
        return tcpRules;
    }

    public void setTcpRules(java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.TcpRules> tcpRules) {
        this.tcpRules = tcpRules;
    }
}

