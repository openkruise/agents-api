package io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"name","webhook","when"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Audit implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Name uniquely identifies this action within its containing list.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    @io.fabric8.generator.annotation.Required()
    @io.fabric8.generator.annotation.Pattern("^[a-z0-9]([-a-z0-9]*[a-z0-9])?$")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Name uniquely identifies this action within its containing list.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Webhook is the destination for this action.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("webhook")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Webhook is the destination for this action.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.audit.Webhook webhook;

    public io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.audit.Webhook getWebhook() {
        return webhook;
    }

    public void setWebhook(io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.audit.Webhook webhook) {
        this.webhook = webhook;
    }

    /**
     * When is a CEL expression that determines whether the event is emitted.
     * It must evaluate to a boolean. An empty expression evaluates to true.
     *
     * A compilation error prevents the profile from compiling. A runtime
     * evaluation error drops the event.
     *
     * Available variables:
     *   result   string                  one of passthrough/mutated/blocked/bypassed/error
     *   request  map<string, dyn>        host, port, path, method, scheme, headers, queryParams
     *   pod      map<string, dyn>        name, namespace, ip, labels
     *   profile  map<string, string>     name, namespace
     *   rule     map<string, string>     name (the matched rule's name)
     *   inputs   map<string, dyn>        profile-scoped inputs
     *   response map<string, dyn>        status
     *
     * Examples:
     *   result == "blocked"
     *   result in ["blocked", "bypassed"]
     *   pod.labels["team"] == "fraud" && result != "passthrough"
     *   rule.name.startsWith("pii-")
     */
    @com.fasterxml.jackson.annotation.JsonProperty("when")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("When is a CEL expression that determines whether the event is emitted.\nIt must evaluate to a boolean. An empty expression evaluates to true.\n\nA compilation error prevents the profile from compiling. A runtime\nevaluation error drops the event.\n\nAvailable variables:\n  result   string                  one of passthrough/mutated/blocked/bypassed/error\n  request  map<string, dyn>        host, port, path, method, scheme, headers, queryParams\n  pod      map<string, dyn>        name, namespace, ip, labels\n  profile  map<string, string>     name, namespace\n  rule     map<string, string>     name (the matched rule's name)\n  inputs   map<string, dyn>        profile-scoped inputs\n  response map<string, dyn>        status\n\nExamples:\n  result == \"blocked\"\n  result in [\"blocked\", \"bypassed\"]\n  pod.labels[\"team\"] == \"fraud\" && result != \"passthrough\"\n  rule.name.startsWith(\"pii-\")")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String when;

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }
}

