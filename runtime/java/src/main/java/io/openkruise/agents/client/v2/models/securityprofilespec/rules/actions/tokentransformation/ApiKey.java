package io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"targetHeader","valueTemplate","when"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class ApiKey implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * TargetHeader is the request header to overwrite with the new token.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("targetHeader")
    @io.fabric8.generator.annotation.Pattern("^[A-Za-z0-9!#$%&'*+\\-.^_|~]+$")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TargetHeader is the request header to overwrite with the new token.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String targetHeader = "Authorization";

    public String getTargetHeader() {
        return targetHeader;
    }

    public void setTargetHeader(String targetHeader) {
        this.targetHeader = targetHeader;
    }

    /**
     * ValueTemplate is a Go text/template for the header value.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("valueTemplate")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ValueTemplate is a Go text/template for the header value.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String valueTemplate;

    public String getValueTemplate() {
        return valueTemplate;
    }

    public void setValueTemplate(String valueTemplate) {
        this.valueTemplate = valueTemplate;
    }

    /**
     * When is an optional condition; the transformation is skipped if
     * the header does not match.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("when")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("When is an optional condition; the transformation is skipped if\nthe header does not match.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.apikey.When when;

    public io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.apikey.When getWhen() {
        return when;
    }

    public void setWhen(io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.apikey.When when) {
        this.when = when;
    }
}

