package io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.oauth2;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"authorizationRequired","error","success"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class CustomResponse implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * AuthorizationRequired adds fields when the service confirms a
     * non-retryable need for new authorization. Its fields override matching
     * application fields in Error, while standard OAuth fields remain protected.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("authorizationRequired")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("AuthorizationRequired adds fields when the service confirms a\nnon-retryable need for new authorization. Its fields override matching\napplication fields in Error, while standard OAuth fields remain protected.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.oauth2.customresponse.AuthorizationRequired authorizationRequired;

    public io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.oauth2.customresponse.AuthorizationRequired getAuthorizationRequired() {
        return authorizationRequired;
    }

    public void setAuthorizationRequired(io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.oauth2.customresponse.AuthorizationRequired authorizationRequired) {
        this.authorizationRequired = authorizationRequired;
    }

    /**
     * Error adds fields to an OAuth error reply for any operation.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("error")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Error adds fields to an OAuth error reply for any operation.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.oauth2.customresponse.Error error;

    public io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.oauth2.customresponse.Error getError() {
        return error;
    }

    public void setError(io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.oauth2.customresponse.Error error) {
        this.error = error;
    }

    /**
     * Success adds fields to a successful token reply. Only Token supports this;
     * device authorization success and upstream resource replies are unchanged.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("success")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Success adds fields to a successful token reply. Only Token supports this;\ndevice authorization success and upstream resource replies are unchanged.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.oauth2.customresponse.Success success;

    public io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.oauth2.customresponse.Success getSuccess() {
        return success;
    }

    public void setSuccess(io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions.tokentransformation.oauth2.customresponse.Success success) {
        this.success = success;
    }
}

