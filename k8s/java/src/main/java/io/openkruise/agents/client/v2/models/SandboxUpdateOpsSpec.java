package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"lifecycle","patch","paused","selector","stateFilter","updateStrategy"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class SandboxUpdateOpsSpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Lifecycle defines pre/post upgrade hooks to set on each sandbox during upgrade.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("lifecycle")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Lifecycle defines pre/post upgrade hooks to set on each sandbox during upgrade.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxupdateopsspec.Lifecycle lifecycle;

    public io.openkruise.agents.client.v2.models.sandboxupdateopsspec.Lifecycle getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(io.openkruise.agents.client.v2.models.sandboxupdateopsspec.Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    /**
     * Patch defines the changes to apply to each selected sandbox's template.
     * The patch is applied as a Strategic Merge Patch on the sandbox's PodTemplateSpec.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("patch")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Patch defines the changes to apply to each selected sandbox's template.\nThe patch is applied as a Strategic Merge Patch on the sandbox's PodTemplateSpec.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.fabric8.kubernetes.api.model.runtime.RawExtension patch;

    public io.fabric8.kubernetes.api.model.runtime.RawExtension getPatch() {
        return patch;
    }

    public void setPatch(io.fabric8.kubernetes.api.model.runtime.RawExtension patch) {
        this.patch = patch;
    }

    /**
     * Paused indicates whether the update operation is paused.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("paused")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Paused indicates whether the update operation is paused.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Boolean paused;

    public Boolean getPaused() {
        return paused;
    }

    public void setPaused(Boolean paused) {
        this.paused = paused;
    }

    /**
     * Selector selects the target sandboxes to update by label.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("selector")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Selector selects the target sandboxes to update by label.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxupdateopsspec.Selector selector;

    public io.openkruise.agents.client.v2.models.sandboxupdateopsspec.Selector getSelector() {
        return selector;
    }

    public void setSelector(io.openkruise.agents.client.v2.models.sandboxupdateopsspec.Selector selector) {
        this.selector = selector;
    }

    /**
     * StateFilter specifies which sandbox states are eligible as upgrade
     * candidates. When empty, defaults to [Running].
     * Upgrading sandboxes are always tracked regardless of this field, as they
     * represent in-progress upgrades owned by this ops.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("stateFilter")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("StateFilter specifies which sandbox states are eligible as upgrade\ncandidates. When empty, defaults to [Running].\nUpgrading sandboxes are always tracked regardless of this field, as they\nrepresent in-progress upgrades owned by this ops.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxupdateopsspec.StateFilter stateFilter;

    public io.openkruise.agents.client.v2.models.sandboxupdateopsspec.StateFilter getStateFilter() {
        return stateFilter;
    }

    public void setStateFilter(io.openkruise.agents.client.v2.models.sandboxupdateopsspec.StateFilter stateFilter) {
        this.stateFilter = stateFilter;
    }

    /**
     * UpdateStrategy defines the strategy for the batch update.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("updateStrategy")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("UpdateStrategy defines the strategy for the batch update.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxupdateopsspec.UpdateStrategy updateStrategy;

    public io.openkruise.agents.client.v2.models.sandboxupdateopsspec.UpdateStrategy getUpdateStrategy() {
        return updateStrategy;
    }

    public void setUpdateStrategy(io.openkruise.agents.client.v2.models.sandboxupdateopsspec.UpdateStrategy updateStrategy) {
        this.updateStrategy = updateStrategy;
    }
}

