package io.openkruise.agents.client.v2.models.sandboxspec.autopausepolicy;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"onIngressTraffic","whenProbedScheduleTime"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Resume implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * OnIngressTraffic resumes the sandbox when the sandbox-gateway receives
     * inbound traffic addressed to it while it is paused. Unlike the probed
     * rules this one is event-driven: it needs no probe, it produces no
     * Status.Schedules entry, and it is executed by the sandbox-gateway rather
     * than by the sandbox controller.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("onIngressTraffic")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("OnIngressTraffic resumes the sandbox when the sandbox-gateway receives\ninbound traffic addressed to it while it is paused. Unlike the probed\nrules this one is event-driven: it needs no probe, it produces no\nStatus.Schedules entry, and it is executed by the sandbox-gateway rather\nthan by the sandbox controller.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxspec.autopausepolicy.resume.OnIngressTraffic onIngressTraffic;

    public io.openkruise.agents.client.v2.models.sandboxspec.autopausepolicy.resume.OnIngressTraffic getOnIngressTraffic() {
        return onIngressTraffic;
    }

    public void setOnIngressTraffic(io.openkruise.agents.client.v2.models.sandboxspec.autopausepolicy.resume.OnIngressTraffic onIngressTraffic) {
        this.onIngressTraffic = onIngressTraffic;
    }

    /**
     * WhenProbedScheduleTime resumes the sandbox before a scheduled task
     * by parsing the probe's Condition message as a timestamp.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("whenProbedScheduleTime")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("WhenProbedScheduleTime resumes the sandbox before a scheduled task\nby parsing the probe's Condition message as a timestamp.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxspec.autopausepolicy.resume.WhenProbedScheduleTime whenProbedScheduleTime;

    public io.openkruise.agents.client.v2.models.sandboxspec.autopausepolicy.resume.WhenProbedScheduleTime getWhenProbedScheduleTime() {
        return whenProbedScheduleTime;
    }

    public void setWhenProbedScheduleTime(io.openkruise.agents.client.v2.models.sandboxspec.autopausepolicy.resume.WhenProbedScheduleTime whenProbedScheduleTime) {
        this.whenProbedScheduleTime = whenProbedScheduleTime;
    }
}

