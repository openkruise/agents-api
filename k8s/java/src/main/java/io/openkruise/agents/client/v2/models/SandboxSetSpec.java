package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"autoPausePolicy","pauseStrategy","persistentContents","probes","replicas","runtimes","scaleStrategy","template","templateRef","updateStrategy","volumeClaimTemplates"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class SandboxSetSpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * AutoPausePolicy defines the pause/resume decision rules for sandboxes
     * created from this SandboxSet. Probe-driven rules reference probes by
     * name (so they are normally set together with spec.probes); the
     * OnIngressTraffic resume rule is event-driven and does not require a
     * probe. The policy is copied verbatim onto each created Sandbox and is
     * part of the update revision hash, so changing it triggers a rolling
     * update of already-created pool sandboxes.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("autoPausePolicy")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("AutoPausePolicy defines the pause/resume decision rules for sandboxes\ncreated from this SandboxSet. Probe-driven rules reference probes by\nname (so they are normally set together with spec.probes); the\nOnIngressTraffic resume rule is event-driven and does not require a\nprobe. The policy is copied verbatim onto each created Sandbox and is\npart of the update revision hash, so changing it triggers a rolling\nupdate of already-created pool sandboxes.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxsetspec.AutoPausePolicy autoPausePolicy;

    public io.openkruise.agents.client.v2.models.sandboxsetspec.AutoPausePolicy getAutoPausePolicy() {
        return autoPausePolicy;
    }

    public void setAutoPausePolicy(io.openkruise.agents.client.v2.models.sandboxsetspec.AutoPausePolicy autoPausePolicy) {
        this.autoPausePolicy = autoPausePolicy;
    }

    /**
     * PauseStrategy configures how sandboxes created from this SandboxSet are
     * paused when their spec.paused is true. It is copied verbatim onto each
     * created Sandbox and is part of the update revision hash, so changing it
     * triggers a rolling update of already-created pool sandboxes.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("pauseStrategy")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("PauseStrategy configures how sandboxes created from this SandboxSet are\npaused when their spec.paused is true. It is copied verbatim onto each\ncreated Sandbox and is part of the update revision hash, so changing it\ntriggers a rolling update of already-created pool sandboxes.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxsetspec.PauseStrategy pauseStrategy;

    public io.openkruise.agents.client.v2.models.sandboxsetspec.PauseStrategy getPauseStrategy() {
        return pauseStrategy;
    }

    public void setPauseStrategy(io.openkruise.agents.client.v2.models.sandboxsetspec.PauseStrategy pauseStrategy) {
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
     * Probes defines the named probes that sandboxes created from this SandboxSet
     * run while they are Running. It is copied verbatim onto each created Sandbox
     * and is part of the update revision hash, so changing it triggers a rolling
     * update of already-created pool sandboxes.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("probes")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Probes defines the named probes that sandboxes created from this SandboxSet\nrun while they are Running. It is copied verbatim onto each created Sandbox\nand is part of the update revision hash, so changing it triggers a rolling\nupdate of already-created pool sandboxes.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.sandboxsetspec.Probes> probes;

    public java.util.List<io.openkruise.agents.client.v2.models.sandboxsetspec.Probes> getProbes() {
        return probes;
    }

    public void setProbes(java.util.List<io.openkruise.agents.client.v2.models.sandboxsetspec.Probes> probes) {
        this.probes = probes;
    }

    /**
     * Replicas is the number of unused sandboxes, including available and creating ones.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("replicas")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Replicas is the number of unused sandboxes, including available and creating ones.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer replicas;

    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    /**
     * Runtimes - Runtime configuration for sandbox object
     */
    @com.fasterxml.jackson.annotation.JsonProperty("runtimes")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Runtimes - Runtime configuration for sandbox object")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.sandboxsetspec.Runtimes> runtimes;

    public java.util.List<io.openkruise.agents.client.v2.models.sandboxsetspec.Runtimes> getRuntimes() {
        return runtimes;
    }

    public void setRuntimes(java.util.List<io.openkruise.agents.client.v2.models.sandboxsetspec.Runtimes> runtimes) {
        this.runtimes = runtimes;
    }

    /**
     * ScaleStrategy indicates the ScaleStrategy that will be employed to
     * create and delete Sandboxes in the SandboxSet.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("scaleStrategy")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ScaleStrategy indicates the ScaleStrategy that will be employed to\ncreate and delete Sandboxes in the SandboxSet.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxsetspec.ScaleStrategy scaleStrategy;

    public io.openkruise.agents.client.v2.models.sandboxsetspec.ScaleStrategy getScaleStrategy() {
        return scaleStrategy;
    }

    public void setScaleStrategy(io.openkruise.agents.client.v2.models.sandboxsetspec.ScaleStrategy scaleStrategy) {
        this.scaleStrategy = scaleStrategy;
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
    private io.openkruise.agents.client.v2.models.sandboxsetspec.TemplateRef templateRef;

    public io.openkruise.agents.client.v2.models.sandboxsetspec.TemplateRef getTemplateRef() {
        return templateRef;
    }

    public void setTemplateRef(io.openkruise.agents.client.v2.models.sandboxsetspec.TemplateRef templateRef) {
        this.templateRef = templateRef;
    }

    /**
     * UpdateStrategy indicates the strategy that will be employed to
     * update Sandboxes in the SandboxSet when the template changes.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("updateStrategy")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("UpdateStrategy indicates the strategy that will be employed to\nupdate Sandboxes in the SandboxSet when the template changes.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxsetspec.UpdateStrategy updateStrategy;

    public io.openkruise.agents.client.v2.models.sandboxsetspec.UpdateStrategy getUpdateStrategy() {
        return updateStrategy;
    }

    public void setUpdateStrategy(io.openkruise.agents.client.v2.models.sandboxsetspec.UpdateStrategy updateStrategy) {
        this.updateStrategy = updateStrategy;
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

