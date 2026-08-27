package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"availableReplicas","conditions","currentRevision","observedGeneration","replicas","selector","updateRevision","updatedAvailableReplicas","updatedReplicas"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class SandboxSetStatus implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * AvailableReplicas is the number of available sandboxes, which are ready to be claimed.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("availableReplicas")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("AvailableReplicas is the number of available sandboxes, which are ready to be claimed.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer availableReplicas;

    public Integer getAvailableReplicas() {
        return availableReplicas;
    }

    public void setAvailableReplicas(Integer availableReplicas) {
        this.availableReplicas = availableReplicas;
    }

    /**
     * conditions represent the current state of the SandboxSet resource.
     * Each condition has a unique type and reflects the status of a specific aspect of the resource.
     * The status of each condition is one of True, False, or Unknown.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("conditions")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("conditions represent the current state of the SandboxSet resource.\nEach condition has a unique type and reflects the status of a specific aspect of the resource.\nThe status of each condition is one of True, False, or Unknown.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.sandboxsetstatus.Conditions> conditions;

    public java.util.List<io.openkruise.agents.client.v2.models.sandboxsetstatus.Conditions> getConditions() {
        return conditions;
    }

    public void setConditions(java.util.List<io.openkruise.agents.client.v2.models.sandboxsetstatus.Conditions> conditions) {
        this.conditions = conditions;
    }

    /**
     * CurrentRevision is the SandboxTemplate name corresponding to the currently
     * materialised revision, or spec.templateRef.Name when templateRef is used.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("currentRevision")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("CurrentRevision is the SandboxTemplate name corresponding to the currently\nmaterialised revision, or spec.templateRef.Name when templateRef is used.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String currentRevision;

    public String getCurrentRevision() {
        return currentRevision;
    }

    public void setCurrentRevision(String currentRevision) {
        this.currentRevision = currentRevision;
    }

    /**
     * observedGeneration is the most recent generation observed for this SandboxSet. It corresponds to the
     * SandboxSet's generation, which is updated on mutation by the API Server.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("observedGeneration")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("observedGeneration is the most recent generation observed for this SandboxSet. It corresponds to the\nSandboxSet's generation, which is updated on mutation by the API Server.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Long observedGeneration;

    public Long getObservedGeneration() {
        return observedGeneration;
    }

    public void setObservedGeneration(Long observedGeneration) {
        this.observedGeneration = observedGeneration;
    }

    /**
     * Replicas is the total number of creating, available, running and paused sandboxes.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("replicas")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Replicas is the total number of creating, available, running and paused sandboxes.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer replicas;

    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    /**
     * Selector is a label query over pods that should match the replica count.
     * This is same as the label selector but in the string format to avoid
     * duplication for CRDs that do not support structural schemas.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("selector")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Selector is a label query over pods that should match the replica count.\nThis is same as the label selector but in the string format to avoid\nduplication for CRDs that do not support structural schemas.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String selector;

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    /**
     * UpdateRevision is the FNV-32 hash computed from spec.template,
     * spec.persistentContents, spec.runtimes, and spec.pauseStrategy.
     * It represents the latest desired template version.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("updateRevision")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("UpdateRevision is the FNV-32 hash computed from spec.template,\nspec.persistentContents, spec.runtimes, and spec.pauseStrategy.\nIt represents the latest desired template version.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String updateRevision;

    public String getUpdateRevision() {
        return updateRevision;
    }

    public void setUpdateRevision(String updateRevision) {
        this.updateRevision = updateRevision;
    }

    /**
     * UpdatedAvailableReplicas is the number of updated sandboxes that are available.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("updatedAvailableReplicas")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("UpdatedAvailableReplicas is the number of updated sandboxes that are available.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer updatedAvailableReplicas;

    public Integer getUpdatedAvailableReplicas() {
        return updatedAvailableReplicas;
    }

    public void setUpdatedAvailableReplicas(Integer updatedAvailableReplicas) {
        this.updatedAvailableReplicas = updatedAvailableReplicas;
    }

    /**
     * UpdatedReplicas is the number of sandboxes that have been updated to the UpdateRevision.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("updatedReplicas")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("UpdatedReplicas is the number of sandboxes that have been updated to the UpdateRevision.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer updatedReplicas;

    public Integer getUpdatedReplicas() {
        return updatedReplicas;
    }

    public void setUpdatedReplicas(Integer updatedReplicas) {
        this.updatedReplicas = updatedReplicas;
    }
}

