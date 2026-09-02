package io.openkruise.agents.client.v2.models.sandboxspec.autopausepolicy.resume;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"leadTime","probe","timeFormat"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class WhenProbedScheduleTime implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * LeadTime is the duration before the parsed timestamp at which the
     * sandbox should be resumed. For example, if the probe reports the
     * next scheduled task at time T and LeadTime is 5m, the sandbox is
     * resumed at T - 5m.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("leadTime")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("LeadTime is the duration before the parsed timestamp at which the\nsandbox should be resumed. For example, if the probe reports the\nnext scheduled task at time T and LeadTime is 5m, the sandbox is\nresumed at T - 5m.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String leadTime = "5m";

    public String getLeadTime() {
        return leadTime;
    }

    public void setLeadTime(String leadTime) {
        this.leadTime = leadTime;
    }

    /**
     * Probe is the name of the probe to evaluate for resume decisions.
     * Must match a probe name in Spec.Probes.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("probe")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Probe is the name of the probe to evaluate for resume decisions.\nMust match a probe name in Spec.Probes.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String probe;

    public String getProbe() {
        return probe;
    }

    public void setProbe(String probe) {
        this.probe = probe;
    }

    public enum TimeFormat {

        @com.fasterxml.jackson.annotation.JsonProperty("unix")
        UNIX("unix"), @com.fasterxml.jackson.annotation.JsonProperty("datetime")
        DATETIME("datetime");

        java.lang.String value;

        TimeFormat(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * TimeFormat indicates the format of the probe's Condition message for
     * parsing as a timestamp, and defaults to "unix":
     *
     *   unix     - seconds since epoch, e.g. "1787040000"
     *   datetime - RFC3339 with offset, e.g. "2026-08-29T08:00:00+08:00"
     *
     * NextResumeTime is set to the parsed timestamp minus LeadTime.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("timeFormat")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TimeFormat indicates the format of the probe's Condition message for\nparsing as a timestamp, and defaults to \"unix\":\n\n  unix     - seconds since epoch, e.g. \"1787040000\"\n  datetime - RFC3339 with offset, e.g. \"2026-08-29T08:00:00+08:00\"\n\nNextResumeTime is set to the parsed timestamp minus LeadTime.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private TimeFormat timeFormat = io.fabric8.kubernetes.client.utils.Serialization.unmarshal("\"unix\"", TimeFormat.class);

    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(TimeFormat timeFormat) {
        this.timeFormat = timeFormat;
    }
}

