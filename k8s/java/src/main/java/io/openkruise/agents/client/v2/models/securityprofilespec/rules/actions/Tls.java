package io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"action"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Tls implements io.fabric8.kubernetes.api.model.KubernetesResource {

    public enum Action {

        @com.fasterxml.jackson.annotation.JsonProperty("Deny")
        DENY("Deny"), @com.fasterxml.jackson.annotation.JsonProperty("Passthrough")
        PASSTHROUGH("Passthrough");

        java.lang.String value;

        Action(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * Action is required and has no default. Deny rejects the connection;
     * it does not promise an HTTP response or a specific TLS alert.
     * Passthrough forwards without decryption or HTTP rule processing.
     * Both actions stop further TLS rule selection and skip TCPRules.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("action")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Action is required and has no default. Deny rejects the connection;\nit does not promise an HTTP response or a specific TLS alert.\nPassthrough forwards without decryption or HTTP rule processing.\nBoth actions stop further TLS rule selection and skip TCPRules.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Action action;

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}

