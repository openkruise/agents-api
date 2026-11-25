package io.openkruise.agents.client.v2.models.sandboxtemplatespec.autopausepolicy.resume;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"pauseTimeout","requestMatches"})
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

    /**
     * RequestMatches restricts which inbound requests may wake the paused
     * sandbox. When empty, every authenticated request addressed to the
     * sandbox wakes it. When set, a request wakes the sandbox if it
     * satisfies any entry; a request that satisfies none is rejected the
     * same way as for a sandbox without wake-on-traffic, and the sandbox
     * stays paused. Requests to a Running sandbox are never filtered.
     *
     * For example, one entry matching sec-fetch-mode=navigate limits wake
     * from browsers to page navigations, so script-initiated reconnects from
     * a page left open in the background do not keep waking the sandbox; a
     * second entry matching a custom header lets SDK or webhook clients that
     * send it still wake the sandbox.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("requestMatches")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("RequestMatches restricts which inbound requests may wake the paused\nsandbox. When empty, every authenticated request addressed to the\nsandbox wakes it. When set, a request wakes the sandbox if it\nsatisfies any entry; a request that satisfies none is rejected the\nsame way as for a sandbox without wake-on-traffic, and the sandbox\nstays paused. Requests to a Running sandbox are never filtered.\n\nFor example, one entry matching sec-fetch-mode=navigate limits wake\nfrom browsers to page navigations, so script-initiated reconnects from\na page left open in the background do not keep waking the sandbox; a\nsecond entry matching a custom header lets SDK or webhook clients that\nsend it still wake the sandbox.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.sandboxtemplatespec.autopausepolicy.resume.oningresstraffic.RequestMatches> requestMatches;

    public java.util.List<io.openkruise.agents.client.v2.models.sandboxtemplatespec.autopausepolicy.resume.oningresstraffic.RequestMatches> getRequestMatches() {
        return requestMatches;
    }

    public void setRequestMatches(java.util.List<io.openkruise.agents.client.v2.models.sandboxtemplatespec.autopausepolicy.resume.oningresstraffic.RequestMatches> requestMatches) {
        this.requestMatches = requestMatches;
    }
}

