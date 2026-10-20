package io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"customResponse","operation"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Oauth2 implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * CustomResponse adds public application fields to generated OAuth replies.
     * It cannot replace standard OAuth fields or change upstream resource replies.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("customResponse")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("CustomResponse adds public application fields to generated OAuth replies.\nIt cannot replace standard OAuth fields or change upstream resource replies.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.oauth2.CustomResponse customResponse;

    public io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.oauth2.CustomResponse getCustomResponse() {
        return customResponse;
    }

    public void setCustomResponse(io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.oauth2.CustomResponse customResponse) {
        this.customResponse = customResponse;
    }

    /**
     * Operation contains exactly one endpoint role and its options.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("operation")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Operation contains exactly one endpoint role and its options.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.oauth2.Operation operation;

    public io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.oauth2.Operation getOperation() {
        return operation;
    }

    public void setOperation(io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.oauth2.Operation operation) {
        this.operation = operation;
    }
}

