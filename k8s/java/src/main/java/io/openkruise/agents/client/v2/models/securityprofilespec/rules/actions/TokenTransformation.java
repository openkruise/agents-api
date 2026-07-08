package io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"apiKey","credentialRef","disabled","failStrategy","type"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class TokenTransformation implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * ApiKey holds ApiKey-mode specific configuration.
     * Required when Type == ApiKey, must be unset when Type == AliyunSTS.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("apiKey")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ApiKey holds ApiKey-mode specific configuration.\nRequired when Type == ApiKey, must be unset when Type == AliyunSTS.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.ApiKey apiKey;

    public io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.ApiKey getApiKey() {
        return apiKey;
    }

    public void setApiKey(io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.ApiKey apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * CredentialRef identifies the credential source for this
     * transformation. Required.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("credentialRef")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("CredentialRef identifies the credential source for this\ntransformation. Required.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.CredentialRef credentialRef;

    public io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.CredentialRef getCredentialRef() {
        return credentialRef;
    }

    public void setCredentialRef(io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.CredentialRef credentialRef) {
        this.credentialRef = credentialRef;
    }

    /**
     * Disabled temporarily disables this action without removing its
     * configuration. When true the action is skipped during evaluation.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("disabled")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Disabled temporarily disables this action without removing its\nconfiguration. When true the action is skipped during evaluation.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Boolean disabled = false;

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public enum FailStrategy {

        @com.fasterxml.jackson.annotation.JsonProperty("Allow")
        ALLOW("Allow"), @com.fasterxml.jackson.annotation.JsonProperty("Block")
        BLOCK("Block"), @com.fasterxml.jackson.annotation.JsonProperty("Ignore")
        IGNORE("Ignore");

        java.lang.String value;

        FailStrategy(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * FailStrategy controls behaviour when the transformation fails.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("failStrategy")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("FailStrategy controls behaviour when the transformation fails.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private FailStrategy failStrategy = io.fabric8.kubernetes.client.utils.Serialization.unmarshal("\"Block\"", FailStrategy.class);

    public FailStrategy getFailStrategy() {
        return failStrategy;
    }

    public void setFailStrategy(FailStrategy failStrategy) {
        this.failStrategy = failStrategy;
    }

    public enum Type {

        @com.fasterxml.jackson.annotation.JsonProperty("ApiKey")
        APIKEY("ApiKey"), @com.fasterxml.jackson.annotation.JsonProperty("AliyunSTS")
        ALIYUNSTS("AliyunSTS");

        java.lang.String value;

        Type(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * Type discriminates the transformation strategy. Defaults to ApiKey.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("type")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Type discriminates the transformation strategy. Defaults to ApiKey.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Type type = io.fabric8.kubernetes.client.utils.Serialization.unmarshal("\"ApiKey\"", Type.class);

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}

