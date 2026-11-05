package io.openkruise.agents.client.v2.models.sandboxsetspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"postResume","postUpgrade","prePause","preUpgrade"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Lifecycle implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * PostResume is a best-effort action executed after runtime, credential, and
     * storage initialization during resume. Exhausted retries do not block Ready.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("postResume")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("PostResume is a best-effort action executed after runtime, credential, and\nstorage initialization during resume. Exhausted retries do not block Ready.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxsetspec.lifecycle.PostResume postResume;

    public io.openkruise.agents.client.v2.models.sandboxsetspec.lifecycle.PostResume getPostResume() {
        return postResume;
    }

    public void setPostResume(io.openkruise.agents.client.v2.models.sandboxsetspec.lifecycle.PostResume postResume) {
        this.postResume = postResume;
    }

    /**
     * PostUpgrade is the action executed after the upgrade.
     * It is typically used to restore workspace data.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("postUpgrade")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("PostUpgrade is the action executed after the upgrade.\nIt is typically used to restore workspace data.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxsetspec.lifecycle.PostUpgrade postUpgrade;

    public io.openkruise.agents.client.v2.models.sandboxsetspec.lifecycle.PostUpgrade getPostUpgrade() {
        return postUpgrade;
    }

    public void setPostUpgrade(io.openkruise.agents.client.v2.models.sandboxsetspec.lifecycle.PostUpgrade postUpgrade) {
        this.postUpgrade = postUpgrade;
    }

    /**
     * PrePause is a best-effort action executed while the Sandbox is still
     * running, before checkpointing or Pod deletion. Exhausted retries do not
     * block pause completion.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("prePause")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("PrePause is a best-effort action executed while the Sandbox is still\nrunning, before checkpointing or Pod deletion. Exhausted retries do not\nblock pause completion.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxsetspec.lifecycle.PrePause prePause;

    public io.openkruise.agents.client.v2.models.sandboxsetspec.lifecycle.PrePause getPrePause() {
        return prePause;
    }

    public void setPrePause(io.openkruise.agents.client.v2.models.sandboxsetspec.lifecycle.PrePause prePause) {
        this.prePause = prePause;
    }

    /**
     * PreUpgrade is the action executed before the upgrade.
     * It is typically used to backup workspace data.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("preUpgrade")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("PreUpgrade is the action executed before the upgrade.\nIt is typically used to backup workspace data.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxsetspec.lifecycle.PreUpgrade preUpgrade;

    public io.openkruise.agents.client.v2.models.sandboxsetspec.lifecycle.PreUpgrade getPreUpgrade() {
        return preUpgrade;
    }

    public void setPreUpgrade(io.openkruise.agents.client.v2.models.sandboxsetspec.lifecycle.PreUpgrade preUpgrade) {
        this.preUpgrade = preUpgrade;
    }
}

