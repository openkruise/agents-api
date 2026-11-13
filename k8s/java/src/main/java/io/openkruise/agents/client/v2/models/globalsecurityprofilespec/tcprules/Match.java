package io.openkruise.agents.client.v2.models.globalsecurityprofilespec.tcprules;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"cidrs","ports"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Match implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * CIDRs lists IPv4 or IPv6 addresses or networks. A single address matches
     * only that address (IPv4 /32 or IPv6 /128). Entries are ORed; an omitted
     * or empty list matches any address. Consumers validate address syntax.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("cidrs")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("CIDRs lists IPv4 or IPv6 addresses or networks. A single address matches\nonly that address (IPv4 /32 or IPv6 /128). Entries are ORed; an omitted\nor empty list matches any address. Consumers validate address syntax.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<String> cidrs;

    public java.util.List<String> getCidrs() {
        return cidrs;
    }

    public void setCidrs(java.util.List<String> cidrs) {
        this.cidrs = cidrs;
    }

    /**
     * Ports lists destination ports. Entries are ORed. An omitted or empty
     * list matches any port.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("ports")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Ports lists destination ports. Entries are ORed. An omitted or empty\nlist matches any port.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<Integer> ports;

    public java.util.List<Integer> getPorts() {
        return ports;
    }

    public void setPorts(java.util.List<Integer> ports) {
        this.ports = ports;
    }
}

