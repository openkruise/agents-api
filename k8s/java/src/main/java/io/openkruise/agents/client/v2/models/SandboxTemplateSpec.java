package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"autoPausePolicy","pauseStrategy","persistentContents","probes","runtimes","template","volumeClaimTemplates"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class SandboxTemplateSpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * AutoPausePolicy defines the pause/resume decision rules for sandboxes
     * created from this template. It is only informational when a SandboxSet uses
     * spec.templateRef: SandboxSetSpec.AutoPausePolicy always takes precedence and
     * is what actually reaches created Sandboxes.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("autoPausePolicy")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("AutoPausePolicy defines the pause/resume decision rules for sandboxes\ncreated from this template. It is only informational when a SandboxSet uses\nspec.templateRef: SandboxSetSpec.AutoPausePolicy always takes precedence and\nis what actually reaches created Sandboxes.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxtemplatespec.AutoPausePolicy autoPausePolicy;

    public io.openkruise.agents.client.v2.models.sandboxtemplatespec.AutoPausePolicy getAutoPausePolicy() {
        return autoPausePolicy;
    }

    public void setAutoPausePolicy(io.openkruise.agents.client.v2.models.sandboxtemplatespec.AutoPausePolicy autoPausePolicy) {
        this.autoPausePolicy = autoPausePolicy;
    }

    /**
     * PauseStrategy configures how sandboxes created from this template are
     * paused when their spec.paused is true. It is only informational when a
     * SandboxSet uses spec.templateRef: SandboxSetSpec.PauseStrategy always
     * takes precedence and is what actually reaches created Sandboxes.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("pauseStrategy")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("PauseStrategy configures how sandboxes created from this template are\npaused when their spec.paused is true. It is only informational when a\nSandboxSet uses spec.templateRef: SandboxSetSpec.PauseStrategy always\ntakes precedence and is what actually reaches created Sandboxes.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxtemplatespec.PauseStrategy pauseStrategy;

    public io.openkruise.agents.client.v2.models.sandboxtemplatespec.PauseStrategy getPauseStrategy() {
        return pauseStrategy;
    }

    public void setPauseStrategy(io.openkruise.agents.client.v2.models.sandboxtemplatespec.PauseStrategy pauseStrategy) {
        this.pauseStrategy = pauseStrategy;
    }

    /**
     * PersistentContents indicates resume pod with persistent content, Enum: ip, memory, filesystem
     */
    @com.fasterxml.jackson.annotation.JsonProperty("persistentContents")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("PersistentContents indicates resume pod with persistent content, Enum: ip, memory, filesystem")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<String> persistentContents;

    public java.util.List<String> getPersistentContents() {
        return persistentContents;
    }

    public void setPersistentContents(java.util.List<String> persistentContents) {
        this.persistentContents = persistentContents;
    }

    /**
     * Probes defines the named probes that sandboxes created from this template
     * run while they are Running. It is only informational when a SandboxSet uses
     * spec.templateRef: SandboxSetSpec.Probes always takes precedence and is what
     * actually reaches created Sandboxes.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("probes")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Probes defines the named probes that sandboxes created from this template\nrun while they are Running. It is only informational when a SandboxSet uses\nspec.templateRef: SandboxSetSpec.Probes always takes precedence and is what\nactually reaches created Sandboxes.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.sandboxtemplatespec.Probes> probes;

    public java.util.List<io.openkruise.agents.client.v2.models.sandboxtemplatespec.Probes> getProbes() {
        return probes;
    }

    public void setProbes(java.util.List<io.openkruise.agents.client.v2.models.sandboxtemplatespec.Probes> probes) {
        this.probes = probes;
    }

    /**
     * Runtimes - Runtime configuration for sandbox object
     */
    @com.fasterxml.jackson.annotation.JsonProperty("runtimes")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Runtimes - Runtime configuration for sandbox object")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.sandboxtemplatespec.Runtimes> runtimes;

    public java.util.List<io.openkruise.agents.client.v2.models.sandboxtemplatespec.Runtimes> getRuntimes() {
        return runtimes;
    }

    public void setRuntimes(java.util.List<io.openkruise.agents.client.v2.models.sandboxtemplatespec.Runtimes> runtimes) {
        this.runtimes = runtimes;
    }

    /**
     * Template describes the pods that will be created.
     * Template is mutual exclusive with TemplateRef
     */
    @com.fasterxml.jackson.annotation.JsonProperty("template")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Template describes the pods that will be created.\nTemplate is mutual exclusive with TemplateRef")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.fabric8.kubernetes.api.model.PodTemplateSpec template;

    public io.fabric8.kubernetes.api.model.PodTemplateSpec getTemplate() {
        return template;
    }

    public void setTemplate(io.fabric8.kubernetes.api.model.PodTemplateSpec template) {
        this.template = template;
    }

    /**
     * VolumeClaimTemplates is a list of PVC templates to create for this Sandbox.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("volumeClaimTemplates")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("VolumeClaimTemplates is a list of PVC templates to create for this Sandbox.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.fabric8.kubernetes.api.model.PersistentVolumeClaim> volumeClaimTemplates;

    public java.util.List<io.fabric8.kubernetes.api.model.PersistentVolumeClaim> getVolumeClaimTemplates() {
        return volumeClaimTemplates;
    }

    public void setVolumeClaimTemplates(java.util.List<io.fabric8.kubernetes.api.model.PersistentVolumeClaim> volumeClaimTemplates) {
        this.volumeClaimTemplates = volumeClaimTemplates;
    }
}

