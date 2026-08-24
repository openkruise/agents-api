package io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"audit","block","bypass","headerManipulation","mcpToolPolicy","tokenTransformation"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Actions implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Audit lists rule-specific audit actions. A non-empty list replaces the
     * profile-level Audit list for this rule. An empty list inherits the
     * profile-level list.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("audit")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Audit lists rule-specific audit actions. A non-empty list replaces the\nprofile-level Audit list for this rule. An empty list inherits the\nprofile-level list.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.Audit> audit;

    public java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.Audit> getAudit() {
        return audit;
    }

    public void setAudit(java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.Audit> audit) {
        this.audit = audit;
    }

    /**
     * Block is a terminal action that returns a configured HTTP response
     * to the client without forwarding upstream.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("block")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Block is a terminal action that returns a configured HTTP response\nto the client without forwarding upstream.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.Block block;

    public io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.Block getBlock() {
        return block;
    }

    public void setBlock(io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.Block block) {
        this.block = block;
    }

    /**
     * Bypass forwards the request and skips all remaining actions and rules
     * across matching profiles. False is equivalent to omission.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("bypass")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Bypass forwards the request and skips all remaining actions and rules\nacross matching profiles. False is equivalent to omission.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Boolean bypass;

    public Boolean getBypass() {
        return bypass;
    }

    public void setBypass(Boolean bypass) {
        this.bypass = bypass;
    }

    /**
     * HeaderManipulation sets or removes plaintext request headers.
     * Non-terminal. Values are stored verbatim — use TokenTransformation
     * for credentials.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("headerManipulation")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("HeaderManipulation sets or removes plaintext request headers.\nNon-terminal. Values are stored verbatim — use TokenTransformation\nfor credentials.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.HeaderManipulation headerManipulation;

    public io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.HeaderManipulation getHeaderManipulation() {
        return headerManipulation;
    }

    public void setHeaderManipulation(io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.HeaderManipulation headerManipulation) {
        this.headerManipulation = headerManipulation;
    }

    /**
     * MCPToolPolicy defines inline MCP tool access control rules.
     * Non-terminal when the policy allows; terminal (like Block) when
     * denied.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("mcpToolPolicy")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("MCPToolPolicy defines inline MCP tool access control rules.\nNon-terminal when the policy allows; terminal (like Block) when\ndenied.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.McpToolPolicy mcpToolPolicy;

    public io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.McpToolPolicy getMcpToolPolicy() {
        return mcpToolPolicy;
    }

    public void setMcpToolPolicy(io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.McpToolPolicy mcpToolPolicy) {
        this.mcpToolPolicy = mcpToolPolicy;
    }

    /**
     * TokenTransformation rewrites request credentials.
     * Non-terminal.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("tokenTransformation")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TokenTransformation rewrites request credentials.\nNon-terminal.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.TokenTransformation tokenTransformation;

    public io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.TokenTransformation getTokenTransformation() {
        return tokenTransformation;
    }

    public void setTokenTransformation(io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.TokenTransformation tokenTransformation) {
        this.tokenTransformation = tokenTransformation;
    }
}

