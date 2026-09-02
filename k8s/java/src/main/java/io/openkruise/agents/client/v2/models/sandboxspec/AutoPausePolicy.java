package io.openkruise.agents.client.v2.models.sandboxspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"pause","resume"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class AutoPausePolicy implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Pause defines the pause policy for the sandbox.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("pause")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Pause defines the pause policy for the sandbox.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxspec.autopausepolicy.Pause pause;

    public io.openkruise.agents.client.v2.models.sandboxspec.autopausepolicy.Pause getPause() {
        return pause;
    }

    public void setPause(io.openkruise.agents.client.v2.models.sandboxspec.autopausepolicy.Pause pause) {
        this.pause = pause;
    }

    /**
     * Resume defines the resume policy for the sandbox.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("resume")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Resume defines the resume policy for the sandbox.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxspec.autopausepolicy.Resume resume;

    public io.openkruise.agents.client.v2.models.sandboxspec.autopausepolicy.Resume getResume() {
        return resume;
    }

    public void setResume(io.openkruise.agents.client.v2.models.sandboxspec.autopausepolicy.Resume resume) {
        this.resume = resume;
    }
}

