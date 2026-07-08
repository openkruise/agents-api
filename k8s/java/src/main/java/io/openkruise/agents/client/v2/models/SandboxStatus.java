package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"conditions","message","nodeName","observedGeneration","phase","podInfo","recycledCount","sandboxIp","updateRevision"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class SandboxStatus implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * conditions represent the current state of the Sandbox resource.
     * Each condition has a unique type and reflects the status of a specific aspect of the resource.
     * The status of each condition is one of True, False, or Unknown.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("conditions")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("conditions represent the current state of the Sandbox resource.\nEach condition has a unique type and reflects the status of a specific aspect of the resource.\nThe status of each condition is one of True, False, or Unknown.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.sandboxstatus.Conditions> conditions;

    public java.util.List<io.openkruise.agents.client.v2.models.sandboxstatus.Conditions> getConditions() {
        return conditions;
    }

    public void setConditions(java.util.List<io.openkruise.agents.client.v2.models.sandboxstatus.Conditions> conditions) {
        this.conditions = conditions;
    }

    /**
     * message
     */
    @com.fasterxml.jackson.annotation.JsonProperty("message")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("message")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * NodeName indicates in which node this sandbox is scheduled.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("nodeName")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("NodeName indicates in which node this sandbox is scheduled.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String nodeName;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * observedGeneration is the most recent generation observed for this Sandbox. It corresponds to the
     * Sandbox's generation, which is updated on mutation by the API Server.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("observedGeneration")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("observedGeneration is the most recent generation observed for this Sandbox. It corresponds to the\nSandbox's generation, which is updated on mutation by the API Server.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Long observedGeneration;

    public Long getObservedGeneration() {
        return observedGeneration;
    }

    public void setObservedGeneration(Long observedGeneration) {
        this.observedGeneration = observedGeneration;
    }

    /**
     * Sandbox Phase
     */
    @com.fasterxml.jackson.annotation.JsonProperty("phase")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Sandbox Phase")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String phase;

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    /**
     * Pod Info
     */
    @com.fasterxml.jackson.annotation.JsonProperty("podInfo")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Pod Info")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxstatus.PodInfo podInfo;

    public io.openkruise.agents.client.v2.models.sandboxstatus.PodInfo getPodInfo() {
        return podInfo;
    }

    public void setPodInfo(io.openkruise.agents.client.v2.models.sandboxstatus.PodInfo podInfo) {
        this.podInfo = podInfo;
    }

    /**
     * RecycledCount records the number of times this sandbox has been recycled.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("recycledCount")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("RecycledCount records the number of times this sandbox has been recycled.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer recycledCount;

    public Integer getRecycledCount() {
        return recycledCount;
    }

    public void setRecycledCount(Integer recycledCount) {
        this.recycledCount = recycledCount;
    }

    /**
     * SandboxIp is the ip address allocated to the sandbox.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("sandboxIp")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("SandboxIp is the ip address allocated to the sandbox.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String sandboxIp;

    public String getSandboxIp() {
        return sandboxIp;
    }

    public void setSandboxIp(String sandboxIp) {
        this.sandboxIp = sandboxIp;
    }

    /**
     * UpdateRevision is the template-hash calculated from `spec.template`.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("updateRevision")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("UpdateRevision is the template-hash calculated from `spec.template`.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String updateRevision;

    public String getUpdateRevision() {
        return updateRevision;
    }

    public void setUpdateRevision(String updateRevision) {
        this.updateRevision = updateRevision;
    }
}

