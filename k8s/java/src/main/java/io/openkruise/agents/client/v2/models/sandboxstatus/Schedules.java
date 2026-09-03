package io.openkruise.agents.client.v2.models.sandboxstatus;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"nextPauseTime","nextResumeTime","reason"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Schedules implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * NextPauseTime is when the sandbox is expected to be paused, computed from
     * the pause policy once the probed idle threshold is about to be reached.
     * It is cleared after a pause is triggered.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("nextPauseTime")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("NextPauseTime is when the sandbox is expected to be paused, computed from\nthe pause policy once the probed idle threshold is about to be reached.\nIt is cleared after a pause is triggered.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.time.ZonedDateTime nextPauseTime;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssVV")
    public java.time.ZonedDateTime getNextPauseTime() {
        return nextPauseTime;
    }

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[XXX][VV]")
    public void setNextPauseTime(java.time.ZonedDateTime nextPauseTime) {
        this.nextPauseTime = nextPauseTime;
    }

    /**
     * NextResumeTime is when the sandbox is expected to be resumed, computed from
     * the resume policy's probed schedule time. It is cleared after a resume is triggered.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("nextResumeTime")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("NextResumeTime is when the sandbox is expected to be resumed, computed from\nthe resume policy's probed schedule time. It is cleared after a resume is triggered.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.time.ZonedDateTime nextResumeTime;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssVV")
    public java.time.ZonedDateTime getNextResumeTime() {
        return nextResumeTime;
    }

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[XXX][VV]")
    public void setNextResumeTime(java.time.ZonedDateTime nextResumeTime) {
        this.nextResumeTime = nextResumeTime;
    }

    /**
     * Reason indicates which auto-pause rule triggered this schedule entry.
     * Examples: "probedIdle" (pause triggered by WhenProbedIdleState),
     * "probedSchedule" (resume triggered by WhenProbedScheduleTime).
     */
    @com.fasterxml.jackson.annotation.JsonProperty("reason")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Reason indicates which auto-pause rule triggered this schedule entry.\nExamples: \"probedIdle\" (pause triggered by WhenProbedIdleState),\n\"probedSchedule\" (resume triggered by WhenProbedScheduleTime).")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

