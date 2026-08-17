package io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"domains","headers","methods","paths","ports","queryParams","schemes"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Match implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Domains lists target host names. Supports "*" (any domain) and
     * "*.example.com" wildcard prefixes.
     *
     * CAUTION: wildcard and specific domains can both match the same request
     * under Default Continue semantics, so rule ordering matters.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("domains")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Domains lists target host names. Supports \"*\" (any domain) and\n\"*.example.com\" wildcard prefixes.\n\nCAUTION: wildcard and specific domains can both match the same request\nunder Default Continue semantics, so rule ordering matters.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<String> domains;

    public java.util.List<String> getDomains() {
        return domains;
    }

    public void setDomains(java.util.List<String> domains) {
        this.domains = domains;
    }

    /**
     * Headers lists header matches; multiple entries are ANDed.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("headers")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Headers lists header matches; multiple entries are ANDed.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.match.Headers> headers;

    public java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.match.Headers> getHeaders() {
        return headers;
    }

    public void setHeaders(java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.match.Headers> headers) {
        this.headers = headers;
    }

    public enum Methods {

        @com.fasterxml.jackson.annotation.JsonProperty("GET")
        GET("GET"),
        @com.fasterxml.jackson.annotation.JsonProperty("HEAD")
        HEAD("HEAD"),
        @com.fasterxml.jackson.annotation.JsonProperty("POST")
        POST("POST"),
        @com.fasterxml.jackson.annotation.JsonProperty("PUT")
        PUT("PUT"),
        @com.fasterxml.jackson.annotation.JsonProperty("PATCH")
        PATCH("PATCH"),
        @com.fasterxml.jackson.annotation.JsonProperty("DELETE")
        DELETE("DELETE"),
        @com.fasterxml.jackson.annotation.JsonProperty("OPTIONS")
        OPTIONS("OPTIONS"),
        @com.fasterxml.jackson.annotation.JsonProperty("CONNECT")
        CONNECT("CONNECT"),
        @com.fasterxml.jackson.annotation.JsonProperty("TRACE")
        TRACE("TRACE");

        java.lang.String value;

        Methods(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * Methods lists HTTP methods. Multiple entries are ORed.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("methods")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Methods lists HTTP methods. Multiple entries are ORed.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<Methods> methods;

    public java.util.List<Methods> getMethods() {
        return methods;
    }

    public void setMethods(java.util.List<Methods> methods) {
        this.methods = methods;
    }

    /**
     * Paths lists URL path matches. Multiple entries are ORed. The query
     * string is excluded from path matching.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("paths")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Paths lists URL path matches. Multiple entries are ORed. The query\nstring is excluded from path matching.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.match.Paths> paths;

    public java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.match.Paths> getPaths() {
        return paths;
    }

    public void setPaths(java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.match.Paths> paths) {
        this.paths = paths;
    }

    /**
     * Ports lists destination ports. Multiple entries are ORed.
     *
     * An explicit authority port is used directly. Otherwise, HTTP defaults
     * to 80 and HTTPS defaults to 443. Other schemes have no default port.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("ports")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Ports lists destination ports. Multiple entries are ORed.\n\nAn explicit authority port is used directly. Otherwise, HTTP defaults\nto 80 and HTTPS defaults to 443. Other schemes have no default port.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<Integer> ports;

    public java.util.List<Integer> getPorts() {
        return ports;
    }

    public void setPorts(java.util.List<Integer> ports) {
        this.ports = ports;
    }

    /**
     * QueryParams lists URL query parameter matches. Multiple entries are
     * ANDed. Values are percent-decoded before matching.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("queryParams")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("QueryParams lists URL query parameter matches. Multiple entries are\nANDed. Values are percent-decoded before matching.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.match.QueryParams> queryParams;

    public java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.match.QueryParams> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(java.util.List<io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.match.QueryParams> queryParams) {
        this.queryParams = queryParams;
    }

    /**
     * Schemes lists request schemes, such as "http" and "https". Multiple
     * entries are ORed, and matching is case-insensitive.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("schemes")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Schemes lists request schemes, such as \"http\" and \"https\". Multiple\nentries are ORed, and matching is case-insensitive.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<String> schemes;

    public java.util.List<String> getSchemes() {
        return schemes;
    }

    public void setSchemes(java.util.List<String> schemes) {
        this.schemes = schemes;
    }
}

