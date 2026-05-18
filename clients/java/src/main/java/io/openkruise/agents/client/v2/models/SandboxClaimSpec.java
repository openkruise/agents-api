package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"annotations","claimTimeout","createOnNoStock","dynamicVolumesMount","envVars","inplaceUpdate","labels","replicas","reserveFailedSandbox","runtimes","shutdownTime","skipInitRuntime","templateName","ttlAfterCompleted","waitReadyTimeout"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class SandboxClaimSpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Annotations contains key-value pairs to be added as annotations
     * to claimed Sandbox resources
     */
    @com.fasterxml.jackson.annotation.JsonProperty("annotations")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Annotations contains key-value pairs to be added as annotations\nto claimed Sandbox resources")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.Map<java.lang.String, String> annotations;

    public java.util.Map<java.lang.String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(java.util.Map<java.lang.String, String> annotations) {
        this.annotations = annotations;
    }

    /**
     * ClaimTimeout specifies the maximum duration to wait for claiming sandboxes
     * If the timeout is reached, the claim will be marked as Completed regardless of
     * whether all replicas were successfully claimed
     */
    @com.fasterxml.jackson.annotation.JsonProperty("claimTimeout")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ClaimTimeout specifies the maximum duration to wait for claiming sandboxes\nIf the timeout is reached, the claim will be marked as Completed regardless of\nwhether all replicas were successfully claimed")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String claimTimeout = "1m";

    public String getClaimTimeout() {
        return claimTimeout;
    }

    public void setClaimTimeout(String claimTimeout) {
        this.claimTimeout = claimTimeout;
    }

    /**
     * CreateOnNoStock allows to create new sandbox if no stock available
     */
    @com.fasterxml.jackson.annotation.JsonProperty("createOnNoStock")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("CreateOnNoStock allows to create new sandbox if no stock available")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Boolean createOnNoStock = true;

    public Boolean getCreateOnNoStock() {
        return createOnNoStock;
    }

    public void setCreateOnNoStock(Boolean createOnNoStock) {
        this.createOnNoStock = createOnNoStock;
    }

    /**
     * DynamicVolumesMount specifies the dynamic volumes to be mounted into the sandbox
     */
    @com.fasterxml.jackson.annotation.JsonProperty("dynamicVolumesMount")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("DynamicVolumesMount specifies the dynamic volumes to be mounted into the sandbox")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.sandboxclaimspec.DynamicVolumesMount> dynamicVolumesMount;

    public java.util.List<io.openkruise.agents.client.v2.models.sandboxclaimspec.DynamicVolumesMount> getDynamicVolumesMount() {
        return dynamicVolumesMount;
    }

    public void setDynamicVolumesMount(java.util.List<io.openkruise.agents.client.v2.models.sandboxclaimspec.DynamicVolumesMount> dynamicVolumesMount) {
        this.dynamicVolumesMount = dynamicVolumesMount;
    }

    /**
     * EnvVars contains environment variables to be injected into the sandbox
     * These will be passed to the sandbox's init endpoint (envd) after claiming
     * Only applicable if the SandboxSet has envd enabled
     */
    @com.fasterxml.jackson.annotation.JsonProperty("envVars")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("EnvVars contains environment variables to be injected into the sandbox\nThese will be passed to the sandbox's init endpoint (envd) after claiming\nOnly applicable if the SandboxSet has envd enabled")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.Map<java.lang.String, String> envVars;

    public java.util.Map<java.lang.String, String> getEnvVars() {
        return envVars;
    }

    public void setEnvVars(java.util.Map<java.lang.String, String> envVars) {
        this.envVars = envVars;
    }

    /**
     * InplaceUpdate allows to perform inplace update for sandbox while claiming
     */
    @com.fasterxml.jackson.annotation.JsonProperty("inplaceUpdate")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("InplaceUpdate allows to perform inplace update for sandbox while claiming")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxclaimspec.InplaceUpdate inplaceUpdate;

    public io.openkruise.agents.client.v2.models.sandboxclaimspec.InplaceUpdate getInplaceUpdate() {
        return inplaceUpdate;
    }

    public void setInplaceUpdate(io.openkruise.agents.client.v2.models.sandboxclaimspec.InplaceUpdate inplaceUpdate) {
        this.inplaceUpdate = inplaceUpdate;
    }

    /**
     * Labels contains key-value pairs to be added as labels
     * to claimed Sandbox resources and synced to sandbox template labels.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("labels")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Labels contains key-value pairs to be added as labels\nto claimed Sandbox resources and synced to sandbox template labels.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.Map<java.lang.String, String> labels;

    public java.util.Map<java.lang.String, String> getLabels() {
        return labels;
    }

    public void setLabels(java.util.Map<java.lang.String, String> labels) {
        this.labels = labels;
    }

    /**
     * Replicas specifies how many sandboxes to claim (default: 1)
     * For batch claiming support
     * This field is immutable once set
     */
    @com.fasterxml.jackson.annotation.JsonProperty("replicas")
    @io.fabric8.generator.annotation.Min(1.0)
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Replicas specifies how many sandboxes to claim (default: 1)\nFor batch claiming support\nThis field is immutable once set")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer replicas = 1;

    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    /**
     * Set ReserveFailedSandbox to true to reserve failed sandboxes
     */
    @com.fasterxml.jackson.annotation.JsonProperty("reserveFailedSandbox")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Set ReserveFailedSandbox to true to reserve failed sandboxes")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Boolean reserveFailedSandbox;

    public Boolean getReserveFailedSandbox() {
        return reserveFailedSandbox;
    }

    public void setReserveFailedSandbox(Boolean reserveFailedSandbox) {
        this.reserveFailedSandbox = reserveFailedSandbox;
    }

    /**
     * Runtimes - Runtime configuration for sandbox object
     */
    @com.fasterxml.jackson.annotation.JsonProperty("runtimes")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Runtimes - Runtime configuration for sandbox object")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.sandboxclaimspec.Runtimes> runtimes;

    public java.util.List<io.openkruise.agents.client.v2.models.sandboxclaimspec.Runtimes> getRuntimes() {
        return runtimes;
    }

    public void setRuntimes(java.util.List<io.openkruise.agents.client.v2.models.sandboxclaimspec.Runtimes> runtimes) {
        this.runtimes = runtimes;
    }

    /**
     * ShutdownTime specifies the absolute time when the sandbox should be shut down
     * This will be set as spec.shutdownTime (absolute time) on the Sandbox
     */
    @com.fasterxml.jackson.annotation.JsonProperty("shutdownTime")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ShutdownTime specifies the absolute time when the sandbox should be shut down\nThis will be set as spec.shutdownTime (absolute time) on the Sandbox")
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
     * SkipInitRuntime allows to skip init runtime for sandbox while claiming
     */
    @com.fasterxml.jackson.annotation.JsonProperty("skipInitRuntime")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("SkipInitRuntime allows to skip init runtime for sandbox while claiming")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Boolean skipInitRuntime = false;

    public Boolean getSkipInitRuntime() {
        return skipInitRuntime;
    }

    public void setSkipInitRuntime(Boolean skipInitRuntime) {
        this.skipInitRuntime = skipInitRuntime;
    }

    /**
     * TemplateName specifies which SandboxSet pool to claim from
     */
    @com.fasterxml.jackson.annotation.JsonProperty("templateName")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TemplateName specifies which SandboxSet pool to claim from")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String templateName;

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * TTLAfterCompleted specifies the time to live after the claim reaches Completed phase
     * After this duration, the SandboxClaim will be automatically deleted.
     * Note: Only the SandboxClaim resource will be deleted; the claimed sandboxes will NOT be deleted
     * Set to a negative value (e.g., "-1s") to disable automatic deletion (never delete).
     */
    @com.fasterxml.jackson.annotation.JsonProperty("ttlAfterCompleted")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TTLAfterCompleted specifies the time to live after the claim reaches Completed phase\nAfter this duration, the SandboxClaim will be automatically deleted.\nNote: Only the SandboxClaim resource will be deleted; the claimed sandboxes will NOT be deleted\nSet to a negative value (e.g., \"-1s\") to disable automatic deletion (never delete).")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String ttlAfterCompleted = "60m";

    public String getTtlAfterCompleted() {
        return ttlAfterCompleted;
    }

    public void setTtlAfterCompleted(String ttlAfterCompleted) {
        this.ttlAfterCompleted = ttlAfterCompleted;
    }

    /**
     * WaitReadyTimeout specifies the maximum duration for waiting claimed sandbox ready. Default: 30s.
     * A waiting happens when an inplace update happens, a new sandbox created, etc.
     * Format: duration string (e.g., "3h", "200s", "15m")
     */
    @com.fasterxml.jackson.annotation.JsonProperty("waitReadyTimeout")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("WaitReadyTimeout specifies the maximum duration for waiting claimed sandbox ready. Default: 30s.\nA waiting happens when an inplace update happens, a new sandbox created, etc.\nFormat: duration string (e.g., \"3h\", \"200s\", \"15m\")")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String waitReadyTimeout = "30s";

    public String getWaitReadyTimeout() {
        return waitReadyTimeout;
    }

    public void setWaitReadyTimeout(String waitReadyTimeout) {
        this.waitReadyTimeout = waitReadyTimeout;
    }
}

