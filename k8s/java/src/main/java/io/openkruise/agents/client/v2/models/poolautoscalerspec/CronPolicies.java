package io.openkruise.agents.client.v2.models.poolautoscalerspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"name","schedule","targetReplicas","timeZone"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class CronPolicies implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Name is used to specify the scaling policy.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Name is used to specify the scaling policy.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Schedule is a cron expression that defines when this policy should be executed.
     * Supports standard cron format with 5 fields (minute hour day month weekday).
     */
    @com.fasterxml.jackson.annotation.JsonProperty("schedule")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Schedule is a cron expression that defines when this policy should be executed.\nSupports standard cron format with 5 fields (minute hour day month weekday).")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String schedule;

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    /**
     * TargetReplicas is the desired replicas when this policy executes.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("targetReplicas")
    @io.fabric8.generator.annotation.Required()
    @io.fabric8.generator.annotation.Min(0.0)
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TargetReplicas is the desired replicas when this policy executes.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer targetReplicas;

    public Integer getTargetReplicas() {
        return targetReplicas;
    }

    public void setTargetReplicas(Integer targetReplicas) {
        this.targetReplicas = targetReplicas;
    }

    /**
     * TimeZone is the time zone name for the given schedule, e.g. "Asia/Shanghai", "UTC".
     * If not specified, this will default to the time zone of the autoscaler controller manager process.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("timeZone")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TimeZone is the time zone name for the given schedule, e.g. \"Asia/Shanghai\", \"UTC\".\nIf not specified, this will default to the time zone of the autoscaler controller manager process.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String timeZone;

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}

