package io.openkruise.agents.client.v2.models.securityprofilespec.rules;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"domains","headers","methods","paths","ports","queryParams"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Match implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Domains lists target host names. Supports "*" (any domain) and
     * "*.example.com" wildcard prefixes.
     *
     * CAUTION: wildcard and specific domains can both match the same request
     * under Default Continue semantics, so rule ordering matters. See
     * docs/components/traffix-extension.md.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("domains")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Domains lists target host names. Supports \"*\" (any domain) and\n\"*.example.com\" wildcard prefixes.\n\nCAUTION: wildcard and specific domains can both match the same request\nunder Default Continue semantics, so rule ordering matters. See\ndocs/components/traffix-extension.md.")
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
    private java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.match.Headers> headers;

    public java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.match.Headers> getHeaders() {
        return headers;
    }

    public void setHeaders(java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.match.Headers> headers) {
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
     * Methods filters by HTTP method. Only valid when Paths is also set.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("methods")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Methods filters by HTTP method. Only valid when Paths is also set.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<Methods> methods;

    public java.util.List<Methods> getMethods() {
        return methods;
    }

    public void setMethods(java.util.List<Methods> methods) {
        this.methods = methods;
    }

    /**
     * Paths lists URL path matches; multiple entries are ORed. The path
     * is compared without any "?query" suffix — write QueryParams matches
     * to constrain query parameters.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("paths")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Paths lists URL path matches; multiple entries are ORed. The path\nis compared without any \"?query\" suffix — write QueryParams matches\nto constrain query parameters.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.match.Paths> paths;

    public java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.match.Paths> getPaths() {
        return paths;
    }

    public void setPaths(java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.match.Paths> paths) {
        this.paths = paths;
    }

    /**
     * Ports filters by the port the client targeted on the upstream
     * authority. Multiple entries are ORed.
     *
     * When the request authority spells out a port (e.g.
     * "api.example.com:8443"), that port is used directly. When the client
     * omits the port — relying on the scheme default — the matcher infers
     * 80 for http and 443 for https from the request's :scheme. Listing
     * `ports: [80]` therefore matches both "host:80" and a plain "host"
     * over HTTP. An unrecognized scheme leaves the inferred port at 0,
     * which never matches a non-empty Ports list.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("ports")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Ports filters by the port the client targeted on the upstream\nauthority. Multiple entries are ORed.\n\nWhen the request authority spells out a port (e.g.\n\"api.example.com:8443\"), that port is used directly. When the client\nomits the port — relying on the scheme default — the matcher infers\n80 for http and 443 for https from the request's :scheme. Listing\n`ports: [80]` therefore matches both \"host:80\" and a plain \"host\"\nover HTTP. An unrecognized scheme leaves the inferred port at 0,\nwhich never matches a non-empty Ports list.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<Integer> ports;

    public java.util.List<Integer> getPorts() {
        return ports;
    }

    public void setPorts(java.util.List<Integer> ports) {
        this.ports = ports;
    }

    /**
     * QueryParams lists URL query-parameter matches; multiple entries are
     * ANDed. Matched against the percent-decoded value of the FIRST
     * occurrence of each key.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("queryParams")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("QueryParams lists URL query-parameter matches; multiple entries are\nANDed. Matched against the percent-decoded value of the FIRST\noccurrence of each key.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.match.QueryParams> queryParams;

    public java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.match.QueryParams> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(java.util.List<io.openkruise.agents.client.v2.models.securityprofilespec.rules.match.QueryParams> queryParams) {
        this.queryParams = queryParams;
    }
}

