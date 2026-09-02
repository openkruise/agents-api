package io.openkruise.agents.client.v2.models.poolautoscalerstatus;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"lastScheduleTime","name"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class AppliedCronPolicies implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * LastScheduleTime is the last time the policy was successfully scheduled.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("lastScheduleTime")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("LastScheduleTime is the last time the policy was successfully scheduled.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.time.ZonedDateTime lastScheduleTime;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssVV")
    public java.time.ZonedDateTime getLastScheduleTime() {
        return lastScheduleTime;
    }

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[XXX][VV]")
    public void setLastScheduleTime(java.time.ZonedDateTime lastScheduleTime) {
        this.lastScheduleTime = lastScheduleTime;
    }

    /**
     * Name is the cron policy name.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Name is the cron policy name.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

