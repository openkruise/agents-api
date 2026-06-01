package io.openkruise.agents.client.v2.models.securityprofilespec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"name","webhook","when"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Audit implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Name uniquely identifies this audit entry within its enclosing
     * list. Used in metrics labels and dispatcher dedup. Restricted to
     * label-safe characters so it can flow into Prometheus labels.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    @io.fabric8.generator.annotation.Required()
    @io.fabric8.generator.annotation.Pattern("^[a-z0-9]([-a-z0-9]*[a-z0-9])?$")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Name uniquely identifies this audit entry within its enclosing\nlist. Used in metrics labels and dispatcher dedup. Restricted to\nlabel-safe characters so it can flow into Prometheus labels.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Webhook is the HTTP webhook target. Required for now (no other
     * transports are implemented).
     */
    @com.fasterxml.jackson.annotation.JsonProperty("webhook")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Webhook is the HTTP webhook target. Required for now (no other\ntransports are implemented).")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.audit.Webhook webhook;

    public io.openkruise.agents.client.v2.models.securityprofilespec.audit.Webhook getWebhook() {
        return webhook;
    }

    public void setWebhook(io.openkruise.agents.client.v2.models.securityprofilespec.audit.Webhook webhook) {
        this.webhook = webhook;
    }

    /**
     * When is a CEL expression evaluated against AuditContext at
     * resolution time. The expression must evaluate to a bool; the audit
     * fires when the result is true. Empty (default) means "always fire".
     *
     * Available variables:
     *   result   string                  one of passthrough/mutated/blocked/bypassed/error
     *   request  map<string, dyn>        host, port, path, method, scheme, headers, queryParams
     *   pod      map<string, dyn>        name, namespace, ip, labels
     *   profile  map<string, string>     name, namespace
     *   rule     map<string, string>     name (the matched rule's name)
     *
     * Examples:
     *   result == "blocked"
     *   result in ["blocked", "bypassed"]
     *   pod.labels["team"] == "fraud" && result != "passthrough"
     *   rule.name.startsWith("pii-")
     *
     * Map indexing follows CEL's strict semantics: indexing with an
     * absent key raises an eval error (counted as a drop, not a "false"
     * match). Use `in` for safe presence checks, e.g.
     *   "x-priority" in request.headers && request.headers["x-priority"] == "high"
     *
     * Compilation failures (parse, type-check) cause the enclosing
     * SecurityProfile to be rejected at load time. Runtime evaluation
     * errors are counted under
     * traffix_extension_audit_webhook_dropped_total{reason="when_eval"}
     * and the event is dropped.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("when")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("When is a CEL expression evaluated against AuditContext at\nresolution time. The expression must evaluate to a bool; the audit\nfires when the result is true. Empty (default) means \"always fire\".\n\nAvailable variables:\n  result   string                  one of passthrough/mutated/blocked/bypassed/error\n  request  map<string, dyn>        host, port, path, method, scheme, headers, queryParams\n  pod      map<string, dyn>        name, namespace, ip, labels\n  profile  map<string, string>     name, namespace\n  rule     map<string, string>     name (the matched rule's name)\n\nExamples:\n  result == \"blocked\"\n  result in [\"blocked\", \"bypassed\"]\n  pod.labels[\"team\"] == \"fraud\" && result != \"passthrough\"\n  rule.name.startsWith(\"pii-\")\n\nMap indexing follows CEL's strict semantics: indexing with an\nabsent key raises an eval error (counted as a drop, not a \"false\"\nmatch). Use `in` for safe presence checks, e.g.\n  \"x-priority\" in request.headers && request.headers[\"x-priority\"] == \"high\"\n\nCompilation failures (parse, type-check) cause the enclosing\nSecurityProfile to be rejected at load time. Runtime evaluation\nerrors are counted under\ntraffix_extension_audit_webhook_dropped_total{reason=\"when_eval\"}\nand the event is dropped.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String when;

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }
}

