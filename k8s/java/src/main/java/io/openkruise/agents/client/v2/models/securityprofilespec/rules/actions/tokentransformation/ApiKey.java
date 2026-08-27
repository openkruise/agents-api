package io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"targetHeader","targetHeaders","value","valueTemplate","when"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class ApiKey implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * TargetHeader selects the request header replaced by the legacy
     * single-header mode. It is used only when TargetHeaders is omitted and is
     * otherwise ignored. When omitted in legacy mode, implementations preserve
     * the legacy behavior of targeting Authorization.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("targetHeader")
    @io.fabric8.generator.annotation.Pattern("^[A-Za-z0-9!#$%&'*+\\-.^_|~]+$")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TargetHeader selects the request header replaced by the legacy\nsingle-header mode. It is used only when TargetHeaders is omitted and is\notherwise ignored. When omitted in legacy mode, implementations preserve\nthe legacy behavior of targeting Authorization.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String targetHeader;

    public String getTargetHeader() {
        return targetHeader;
    }

    public void setTargetHeader(String targetHeader) {
        this.targetHeader = targetHeader;
    }

    /**
     * TargetHeaders enables selector mode and selects the request headers to
     * replace. When set, Value supplies the value for every selected header and
     * When, TargetHeader, and ValueTemplate are ignored.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("targetHeaders")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TargetHeaders enables selector mode and selects the request headers to\nreplace. When set, Value supplies the value for every selected header and\nWhen, TargetHeader, and ValueTemplate are ignored.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.apikey.TargetHeaders targetHeaders;

    public io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.apikey.TargetHeaders getTargetHeaders() {
        return targetHeaders;
    }

    public void setTargetHeaders(io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.apikey.TargetHeaders targetHeaders) {
        this.targetHeaders = targetHeaders;
    }

    /**
     * Value produces the replacement value for every header selected by
     * TargetHeaders. It is used only when TargetHeaders is set and is otherwise
     * ignored. Its Cel branch must return a string and may read request, pod,
     * profile, rule, inputs, token, header.name, and header.value. Its Template
     * branch may read Request, Pod, Profile, Rule, Inputs, Token, and Header.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("value")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Value produces the replacement value for every header selected by\nTargetHeaders. It is used only when TargetHeaders is set and is otherwise\nignored. Its Cel branch must return a string and may read request, pod,\nprofile, rule, inputs, token, header.name, and header.value. Its Template\nbranch may read Request, Pod, Profile, Rule, Inputs, Token, and Header.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.apikey.Value value;

    public io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.apikey.Value getValue() {
        return value;
    }

    public void setValue(io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.apikey.Value value) {
        this.value = value;
    }

    /**
     * ValueTemplate renders the replacement value in the legacy single-header
     * mode. It is used only when TargetHeaders is omitted and is otherwise
     * ignored.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("valueTemplate")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ValueTemplate renders the replacement value in the legacy single-header\nmode. It is used only when TargetHeaders is omitted and is otherwise\nignored.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String valueTemplate;

    public String getValueTemplate() {
        return valueTemplate;
    }

    public void setValueTemplate(String valueTemplate) {
        this.valueTemplate = valueTemplate;
    }

    /**
     * When gates the legacy single-header mode on a matching request header.
     * It is used only when TargetHeaders is omitted and is otherwise ignored.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("when")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("When gates the legacy single-header mode on a matching request header.\nIt is used only when TargetHeaders is omitted and is otherwise ignored.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.apikey.When when;

    public io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.apikey.When getWhen() {
        return when;
    }

    public void setWhen(io.openkruise.agents.client.v2.models.securityprofilespec.rules.actions.tokentransformation.apikey.When when) {
        this.when = when;
    }
}

