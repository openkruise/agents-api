package io.openkruise.agents.client.v2.models.sandboxupdateopsspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"states"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class StateFilter implements io.fabric8.kubernetes.api.model.KubernetesResource {

    public enum States {

        @com.fasterxml.jackson.annotation.JsonProperty("Running")
        RUNNING("Running"), @com.fasterxml.jackson.annotation.JsonProperty("Paused")
        PAUSED("Paused");

        java.lang.String value;

        States(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * States specifies which sandbox phases are eligible as upgrade candidates.
     * When empty, defaults to [Running].
     * Supported values: Running, Paused.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("states")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("States specifies which sandbox phases are eligible as upgrade candidates.\nWhen empty, defaults to [Running].\nSupported values: Running, Paused.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<States> states;

    public java.util.List<States> getStates() {
        return states;
    }

    public void setStates(java.util.List<States> states) {
        this.states = states;
    }
}

