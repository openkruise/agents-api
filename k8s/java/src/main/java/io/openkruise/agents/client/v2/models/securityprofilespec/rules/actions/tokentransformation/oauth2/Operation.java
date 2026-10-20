package io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.oauth2;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"deviceAuthorization","resource","token"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Operation implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * DeviceAuthorization creates a device authorization session.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("deviceAuthorization")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("DeviceAuthorization creates a device authorization session.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.oauth2.operation.DeviceAuthorization deviceAuthorization;

    public io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.oauth2.operation.DeviceAuthorization getDeviceAuthorization() {
        return deviceAuthorization;
    }

    public void setDeviceAuthorization(io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.oauth2.operation.DeviceAuthorization deviceAuthorization) {
        this.deviceAuthorization = deviceAuthorization;
    }

    /**
     * Resource injects a service-issued Bearer credential into the request.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("resource")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Resource injects a service-issued Bearer credential into the request.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.oauth2.operation.Resource resource;

    public io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.oauth2.operation.Resource getResource() {
        return resource;
    }

    public void setResource(io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.oauth2.operation.Resource resource) {
        this.resource = resource;
    }

    /**
     * Token processes token grants and returns placeholder credentials.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("token")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Token processes token grants and returns placeholder credentials.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.oauth2.operation.Token token;

    public io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.oauth2.operation.Token getToken() {
        return token;
    }

    public void setToken(io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.oauth2.operation.Token token) {
        this.token = token;
    }
}

