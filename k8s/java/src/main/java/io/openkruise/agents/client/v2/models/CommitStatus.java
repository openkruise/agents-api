package io.openkruise.agents.client.v2.models;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"commitID","completionTime","conditions","phase","startTime"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class CommitStatus implements io.fabric8.kubernetes.api.model.KubernetesResource {

    @com.fasterxml.jackson.annotation.JsonProperty("commitID")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String commitID;

    public String getCommitID() {
        return commitID;
    }

    public void setCommitID(String commitID) {
        this.commitID = commitID;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("completionTime")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.time.ZonedDateTime completionTime;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssVV")
    public java.time.ZonedDateTime getCompletionTime() {
        return completionTime;
    }

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[XXX][VV]")
    public void setCompletionTime(java.time.ZonedDateTime completionTime) {
        this.completionTime = completionTime;
    }

    /**
     * conditions represent the current state of the Commit resource.
     * Each condition has a unique type and reflects the status of a specific aspect of the resource.
     * The status of each condition is one of True, False, or Unknown.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("conditions")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("conditions represent the current state of the Commit resource.\nEach condition has a unique type and reflects the status of a specific aspect of the resource.\nThe status of each condition is one of True, False, or Unknown.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.commitstatus.Conditions> conditions;

    public java.util.List<io.openkruise.agents.client.v2.models.commitstatus.Conditions> getConditions() {
        return conditions;
    }

    public void setConditions(java.util.List<io.openkruise.agents.client.v2.models.commitstatus.Conditions> conditions) {
        this.conditions = conditions;
    }

    public enum Phase {

        @com.fasterxml.jackson.annotation.JsonProperty("Pending")
        PENDING("Pending"), @com.fasterxml.jackson.annotation.JsonProperty("Running")
        RUNNING("Running"), @com.fasterxml.jackson.annotation.JsonProperty("Succeeded")
        SUCCEEDED("Succeeded"), @com.fasterxml.jackson.annotation.JsonProperty("Failed")
        FAILED("Failed");

        java.lang.String value;

        Phase(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    @com.fasterxml.jackson.annotation.JsonProperty("phase")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Phase phase = io.fabric8.kubernetes.client.utils.Serialization.unmarshal("\"Pending\"", Phase.class);

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("startTime")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.time.ZonedDateTime startTime;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssVV")
    public java.time.ZonedDateTime getStartTime() {
        return startTime;
    }

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[XXX][VV]")
    public void setStartTime(java.time.ZonedDateTime startTime) {
        this.startTime = startTime;
    }
}

