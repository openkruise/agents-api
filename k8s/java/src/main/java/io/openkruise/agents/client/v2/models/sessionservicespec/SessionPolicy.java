package io.openkruise.agents.client.v2.models.sessionservicespec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"affinity","expireAfterCreation","expireAfterIdle","maxSessionsPerSandbox","pauseAfterIdle"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class SessionPolicy implements io.fabric8.kubernetes.api.model.KubernetesResource {

    public enum Affinity {

        @com.fasterxml.jackson.annotation.JsonProperty("BestEffort")
        BESTEFFORT("BestEffort"), @com.fasterxml.jackson.annotation.JsonProperty("Strict")
        STRICT("Strict");

        java.lang.String value;

        Affinity(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * Affinity controls reuse of an existing Session binding. BestEffort may
     * reallocate an unavailable target; Strict keeps an existing binding even
     * when its target is unavailable. Deleting the Sandbox CR removes its bindings;
     * later requests with that Session ID return 404. An absent or empty Session ID
     * creates a new Session identity and binding.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("affinity")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Affinity controls reuse of an existing Session binding. BestEffort may\nreallocate an unavailable target; Strict keeps an existing binding even\nwhen its target is unavailable. Deleting the Sandbox CR removes its bindings;\nlater requests with that Session ID return 404. An absent or empty Session ID\ncreates a new Session identity and binding.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Affinity affinity = io.fabric8.kubernetes.client.utils.Serialization.unmarshal("\"BestEffort\"", Affinity.class);

    public Affinity getAffinity() {
        return affinity;
    }

    public void setAffinity(Affinity affinity) {
        this.affinity = affinity;
    }

    /**
     * ExpireAfterCreation removes Session bindings after their fixed lifetime.
     * Requests and rebinding do not extend this lifetime. Empty disables expiry.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("expireAfterCreation")
    @io.fabric8.generator.annotation.Pattern("^([0-9]+(\\.[0-9]+)?(ns|us|ms|s|m|h))+$")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ExpireAfterCreation removes Session bindings after their fixed lifetime.\nRequests and rebinding do not extend this lifetime. Empty disables expiry.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String expireAfterCreation;

    public String getExpireAfterCreation() {
        return expireAfterCreation;
    }

    public void setExpireAfterCreation(String expireAfterCreation) {
        this.expireAfterCreation = expireAfterCreation;
    }

    /**
     * ExpireAfterIdle declares future idle behavior and currently reports Unsupported.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("expireAfterIdle")
    @io.fabric8.generator.annotation.Pattern("^([0-9]+(\\.[0-9]+)?(ns|us|ms|s|m|h))+$")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ExpireAfterIdle declares future idle behavior and currently reports Unsupported.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String expireAfterIdle;

    public String getExpireAfterIdle() {
        return expireAfterIdle;
    }

    public void setExpireAfterIdle(String expireAfterIdle) {
        this.expireAfterIdle = expireAfterIdle;
    }

    /**
     * MaxSessionsPerSandbox omits a user limit when absent; platform budgets apply.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("maxSessionsPerSandbox")
    @io.fabric8.generator.annotation.Max(512.0)
    @io.fabric8.generator.annotation.Min(1.0)
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("MaxSessionsPerSandbox omits a user limit when absent; platform budgets apply.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer maxSessionsPerSandbox;

    public Integer getMaxSessionsPerSandbox() {
        return maxSessionsPerSandbox;
    }

    public void setMaxSessionsPerSandbox(Integer maxSessionsPerSandbox) {
        this.maxSessionsPerSandbox = maxSessionsPerSandbox;
    }

    /**
     * PauseAfterIdle declares future idle behavior and currently reports Unsupported.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("pauseAfterIdle")
    @io.fabric8.generator.annotation.Pattern("^([0-9]+(\\.[0-9]+)?(ns|us|ms|s|m|h))+$")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("PauseAfterIdle declares future idle behavior and currently reports Unsupported.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String pauseAfterIdle;

    public String getPauseAfterIdle() {
        return pauseAfterIdle;
    }

    public void setPauseAfterIdle(String pauseAfterIdle) {
        this.pauseAfterIdle = pauseAfterIdle;
    }
}

