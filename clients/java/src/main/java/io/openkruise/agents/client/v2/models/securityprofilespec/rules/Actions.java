package io.openkruise.agents.client.v2.models.securityprofilespec.rules;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"audit","block","bypass"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Actions implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Audit lists per-rule audit entries. When non-empty, this list
     * REPLACES the profile-level SecurityProfileSpec.Audit list for this
     * rule's matches (override semantics). When empty or omitted, the
     * spec-level list applies. To suppress audit on a specific rule,
     * add a single entry with `when: "false"`.
     *
     * Audit is non-terminal — it never alters the upstream response and
     * does not short-circuit the rule chain.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("audit")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Audit lists per-rule audit entries. When non-empty, this list\nREPLACES the profile-level SecurityProfileSpec.Audit list for this\nrule's matches (override semantics). When empty or omitted, the\nspec-level list applies. To suppress audit on a specific rule,\nadd a single entry with `when: \"false\"`.\n\nAudit is non-terminal — it never alters the upstream response and\ndoes not short-circuit the rule chain.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.Audit> audit;

    public java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.Audit> getAudit() {
        return audit;
    }

    public void setAudit(java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.Audit> audit) {
        this.audit = audit;
    }

    /**
     * Block is a terminal action that returns a configured HTTP response
     * to the client without forwarding upstream.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("block")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Block is a terminal action that returns a configured HTTP response\nto the client without forwarding upstream.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.Block block;

    public io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.Block getBlock() {
        return block;
    }

    public void setBlock(io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.Block block) {
        this.block = block;
    }

    /**
     * Bypass is a terminal action that skips all subsequent rules and
     * forwards the request without further processing. Useful for trusted
     * internal domains.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("bypass")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Bypass is a terminal action that skips all subsequent rules and\nforwards the request without further processing. Useful for trusted\ninternal domains.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Boolean bypass;

    public Boolean getBypass() {
        return bypass;
    }

    public void setBypass(Boolean bypass) {
        this.bypass = bypass;
    }
}

