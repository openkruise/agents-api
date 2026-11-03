package io.openkruise.agents.client.v2.models.credentialproviderspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"client","discovery","grantType","userFederation"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Oauth2 implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Client is the registered application.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("client")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Client is the registered application.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2.Client client;

    public io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2.Client getClient() {
        return client;
    }

    public void setClient(io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2.Client client) {
        this.client = client;
    }

    /**
     * Discovery locates the provider's OAuth endpoints.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("discovery")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Discovery locates the provider's OAuth endpoints.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2.Discovery discovery;

    public io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2.Discovery getDiscovery() {
        return discovery;
    }

    public void setDiscovery(io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2.Discovery discovery) {
        this.discovery = discovery;
    }

    public enum GrantType {

        @com.fasterxml.jackson.annotation.JsonProperty("userFederation")
        USERFEDERATION("userFederation");

        java.lang.String value;

        GrantType(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * GrantType selects the flow. The only supported value is "userFederation":
     * a user approving an agent's access to their own account.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("grantType")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("GrantType selects the flow. The only supported value is \"userFederation\":\na user approving an agent's access to their own account.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private GrantType grantType;

    public GrantType getGrantType() {
        return grantType;
    }

    public void setGrantType(GrantType grantType) {
        this.grantType = grantType;
    }

    /**
     * UserFederation configures the approval flow. Required when GrantType is
     * userFederation.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("userFederation")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("UserFederation configures the approval flow. Required when GrantType is\nuserFederation.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2.UserFederation userFederation;

    public io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2.UserFederation getUserFederation() {
        return userFederation;
    }

    public void setUserFederation(io.openkruise.agents.client.v2.models.credentialproviderspec.oauth2.UserFederation userFederation) {
        this.userFederation = userFederation;
    }
}

