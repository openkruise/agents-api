package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"lifecycle","pauseTime","paused","persistentContents","runtimes","shutdownTime","template","templateRef","upgradePolicy","volumeClaimTemplates"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class SandboxSpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Lifecycle defines lifecycle hooks for sandbox upgrade.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("lifecycle")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Lifecycle defines lifecycle hooks for sandbox upgrade.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxspec.Lifecycle lifecycle;

    public io.openkruise.agents.client.v2.models.sandboxspec.Lifecycle getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(io.openkruise.agents.client.v2.models.sandboxspec.Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    /**
     * PauseTime - Absolute time when the sandbox will be paused automatically.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("pauseTime")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("PauseTime - Absolute time when the sandbox will be paused automatically.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.time.ZonedDateTime pauseTime;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssVV")
    public java.time.ZonedDateTime getPauseTime() {
        return pauseTime;
    }

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[XXX][VV]")
    public void setPauseTime(java.time.ZonedDateTime pauseTime) {
        this.pauseTime = pauseTime;
    }

    /**
     * Paused indicates whether pause the sandbox pod.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("paused")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Paused indicates whether pause the sandbox pod.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Boolean paused;

    public Boolean getPaused() {
        return paused;
    }

    public void setPaused(Boolean paused) {
        this.paused = paused;
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
     * Runtimes - Runtime configuration for sandbox object
     */
    @com.fasterxml.jackson.annotation.JsonProperty("runtimes")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Runtimes - Runtime configuration for sandbox object")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.sandboxspec.Runtimes> runtimes;

    public java.util.List<io.openkruise.agents.client.v2.models.sandboxspec.Runtimes> getRuntimes() {
        return runtimes;
    }

    public void setRuntimes(java.util.List<io.openkruise.agents.client.v2.models.sandboxspec.Runtimes> runtimes) {
        this.runtimes = runtimes;
    }

    /**
     * ShutdownTime - Absolute time when the sandbox is deleted.
     * If a time in the past is provided, the sandbox will be deleted immediately.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("shutdownTime")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ShutdownTime - Absolute time when the sandbox is deleted.\nIf a time in the past is provided, the sandbox will be deleted immediately.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.time.ZonedDateTime shutdownTime;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssVV")
    public java.time.ZonedDateTime getShutdownTime() {
        return shutdownTime;
    }

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[XXX][VV]")
    public void setShutdownTime(java.time.ZonedDateTime shutdownTime) {
        this.shutdownTime = shutdownTime;
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
     * TemplateRef references a SandboxTemplate, which will be used to create the sandbox.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("templateRef")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TemplateRef references a SandboxTemplate, which will be used to create the sandbox.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxspec.TemplateRef templateRef;

    public io.openkruise.agents.client.v2.models.sandboxspec.TemplateRef getTemplateRef() {
        return templateRef;
    }

    public void setTemplateRef(io.openkruise.agents.client.v2.models.sandboxspec.TemplateRef templateRef) {
        this.templateRef = templateRef;
    }

    /**
     * UpgradePolicy defines the upgrade strategy for the sandbox.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("upgradePolicy")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("UpgradePolicy defines the upgrade strategy for the sandbox.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxspec.UpgradePolicy upgradePolicy;

    public io.openkruise.agents.client.v2.models.sandboxspec.UpgradePolicy getUpgradePolicy() {
        return upgradePolicy;
    }

    public void setUpgradePolicy(io.openkruise.agents.client.v2.models.sandboxspec.UpgradePolicy upgradePolicy) {
        this.upgradePolicy = upgradePolicy;
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

