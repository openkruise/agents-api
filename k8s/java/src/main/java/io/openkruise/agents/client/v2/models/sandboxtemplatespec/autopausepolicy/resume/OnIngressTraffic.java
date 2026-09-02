package io.openkruise.agents.client.v2.models.sandboxtemplatespec.autopausepolicy.resume;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"pauseTimeout"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class OnIngressTraffic implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * PauseTimeout is the auto-pause timeout re-armed by a traffic wake: the
     * gateway writes Spec.PauseTime = now + PauseTimeout atomically with
     * Spec.Paused = false, so the woken sandbox has running time before its
     * next auto-pause. It applies only to auto-pause sandboxes (those that
     * already carry Spec.PauseTime); never-timeout and shutdown-only
     * sandboxes keep their timeout mode unchanged.
     * When absent or non-positive, a traffic wake does not re-arm auto-pause:
     * the woken sandbox keeps running until it is paused or deleted again.
     * A positive value is subject to the resume timeout floor
     * (timeout.DefaultMinResumeTimeoutSeconds, currently 300s): values below
     * the floor are raised to it so the fresh PauseTime cannot expire while
     * the sandbox is still resuming.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("pauseTimeout")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("PauseTimeout is the auto-pause timeout re-armed by a traffic wake: the\ngateway writes Spec.PauseTime = now + PauseTimeout atomically with\nSpec.Paused = false, so the woken sandbox has running time before its\nnext auto-pause. It applies only to auto-pause sandboxes (those that\nalready carry Spec.PauseTime); never-timeout and shutdown-only\nsandboxes keep their timeout mode unchanged.\nWhen absent or non-positive, a traffic wake does not re-arm auto-pause:\nthe woken sandbox keeps running until it is paused or deleted again.\nA positive value is subject to the resume timeout floor\n(timeout.DefaultMinResumeTimeoutSeconds, currently 300s): values below\nthe floor are raised to it so the fresh PauseTime cannot expire while\nthe sandbox is still resuming.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String pauseTimeout;

    public String getPauseTimeout() {
        return pauseTimeout;
    }

    public void setPauseTimeout(String pauseTimeout) {
        this.pauseTimeout = pauseTimeout;
    }
}

