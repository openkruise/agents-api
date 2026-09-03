package io.openkruise.agents.client.v2.models.sandboxsetspec.autopausepolicy.pause;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"messageRegex","probe","thresholdDuration"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class WhenProbedIdleState implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * MessageRegex is a regular expression matched against the probe's
     * Condition message (stdout). When the message matches, the Agent is
     * considered inactive. When it does not match, the Agent is considered
     * active and the sandbox stays Running.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("messageRegex")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("MessageRegex is a regular expression matched against the probe's\nCondition message (stdout). When the message matches, the Agent is\nconsidered inactive. When it does not match, the Agent is considered\nactive and the sandbox stays Running.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String messageRegex;

    public String getMessageRegex() {
        return messageRegex;
    }

    public void setMessageRegex(String messageRegex) {
        this.messageRegex = messageRegex;
    }

    /**
     * Probe is the name of the probe to evaluate for pause decisions.
     * Must match a probe name in Spec.Probes.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("probe")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Probe is the name of the probe to evaluate for pause decisions.\nMust match a probe name in Spec.Probes.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String probe;

    public String getProbe() {
        return probe;
    }

    public void setProbe(String probe) {
        this.probe = probe;
    }

    /**
     * ThresholdDuration is the minimum time the probe's Condition message
     * must continuously match MessageRegex before the sandbox is paused.
     * Measured from the Condition's lastTransitionTime.
     *
     * It is required: pausing as soon as a single probe report matches would
     * drop the smoothing this rule exists for, so there is no meaningful
     * default and an unset value is a misconfiguration rather than
     * "pause immediately".
     */
    @com.fasterxml.jackson.annotation.JsonProperty("thresholdDuration")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ThresholdDuration is the minimum time the probe's Condition message\nmust continuously match MessageRegex before the sandbox is paused.\nMeasured from the Condition's lastTransitionTime.\n\nIt is required: pausing as soon as a single probe report matches would\ndrop the smoothing this rule exists for, so there is no meaningful\ndefault and an unset value is a misconfiguration rather than\n\"pause immediately\".")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String thresholdDuration;

    public String getThresholdDuration() {
        return thresholdDuration;
    }

    public void setThresholdDuration(String thresholdDuration) {
        this.thresholdDuration = thresholdDuration;
    }
}

