package io.openkruise.agents.client.v2.models.globalsecurityprofilespec.rules.actions;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"url"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Proxy implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * URL is an absolute http, https, or socks5 proxy URL with an explicit
     * port (1-65535). Hosts may be DNS names, IPv4, or bracketed IPv6.
     * Userinfo, business paths, queries, and fragments are forbidden; a root
     * slash is accepted. HTTPS proxies require certificate verification.
     * The IPv6 alternatives cover eight groups, compressed groups, and an
     * IPv4 tail (two groups). A pattern avoids per-rule CEL URL parsing costs
     * in profiles whose existing rule list is unbounded.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("url")
    @io.fabric8.generator.annotation.Required()
    @io.fabric8.generator.annotation.Pattern("^(?i:http|https|socks5)://([a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*\\.?|\\[(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|::|:(:[0-9a-fA-F]{1,4}){1,7}|([0-9a-fA-F]{1,4}:){1,6}(:[0-9a-fA-F]{1,4}){1}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){5}|([0-9a-fA-F]{1,4}:){1}(:[0-9a-fA-F]{1,4}){6}|(([0-9a-fA-F]{1,4}:){6}|::([0-9a-fA-F]{1,4}:){0,5}|([0-9a-fA-F]{1,4}:){1}:([0-9a-fA-F]{1,4}:){0,4}|([0-9a-fA-F]{1,4}:){2}:([0-9a-fA-F]{1,4}:){0,3}|([0-9a-fA-F]{1,4}:){3}:([0-9a-fA-F]{1,4}:){0,2}|([0-9a-fA-F]{1,4}:){4}:([0-9a-fA-F]{1,4}:){0,1}|([0-9a-fA-F]{1,4}:){5}:([0-9a-fA-F]{1,4}:){0,0})(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])(\\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3})\\]):0*([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])/?$")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("URL is an absolute http, https, or socks5 proxy URL with an explicit\nport (1-65535). Hosts may be DNS names, IPv4, or bracketed IPv6.\nUserinfo, business paths, queries, and fragments are forbidden; a root\nslash is accepted. HTTPS proxies require certificate verification.\nThe IPv6 alternatives cover eight groups, compressed groups, and an\nIPv4 tail (two groups). A pattern avoids per-rule CEL URL parsing costs\nin profiles whose existing rule list is unbounded.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

