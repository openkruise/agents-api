package io.openkruise.agents.client.v2.models.sandboxspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"postUpgrade","preUpgrade"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Lifecycle implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * PostUpgrade is the action executed after the upgrade.
     * It is typically used to restore workspace data.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("postUpgrade")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("PostUpgrade is the action executed after the upgrade.\nIt is typically used to restore workspace data.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxspec.lifecycle.PostUpgrade postUpgrade;

    public io.openkruise.agents.client.v2.models.sandboxspec.lifecycle.PostUpgrade getPostUpgrade() {
        return postUpgrade;
    }

    public void setPostUpgrade(io.openkruise.agents.client.v2.models.sandboxspec.lifecycle.PostUpgrade postUpgrade) {
        this.postUpgrade = postUpgrade;
    }

    /**
     * PreUpgrade is the action executed before the upgrade.
     * It is typically used to backup workspace data.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("preUpgrade")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("PreUpgrade is the action executed before the upgrade.\nIt is typically used to backup workspace data.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxspec.lifecycle.PreUpgrade preUpgrade;

    public io.openkruise.agents.client.v2.models.sandboxspec.lifecycle.PreUpgrade getPreUpgrade() {
        return preUpgrade;
    }

    public void setPreUpgrade(io.openkruise.agents.client.v2.models.sandboxspec.lifecycle.PreUpgrade preUpgrade) {
        this.preUpgrade = preUpgrade;
    }
}

