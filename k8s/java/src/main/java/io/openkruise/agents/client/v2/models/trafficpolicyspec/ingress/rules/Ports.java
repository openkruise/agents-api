package io.openkruise.agents.client.v2.models.trafficpolicyspec.ingress.rules;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"endPort","port","protocol"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Ports implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * EndPort defines the upper bound of a port range (inclusive). When set,
     * the rule matches destination ports from Port to EndPort. Requires Port
     * to be set and must be >= Port.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("endPort")
    @io.fabric8.generator.annotation.Max(65535.0)
    @io.fabric8.generator.annotation.Min(1.0)
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("EndPort defines the upper bound of a port range (inclusive). When set,\nthe rule matches destination ports from Port to EndPort. Requires Port\nto be set and must be >= Port.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer endPort;

    public Integer getEndPort() {
        return endPort;
    }

    public void setEndPort(Integer endPort) {
        this.endPort = endPort;
    }

    /**
     * Port is the destination port number. When nil, the rule applies to all
     * TCP ports.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("port")
    @io.fabric8.generator.annotation.Max(65535.0)
    @io.fabric8.generator.annotation.Min(1.0)
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Port is the destination port number. When nil, the rule applies to all\nTCP ports.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer port;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public enum Protocol {

        @com.fasterxml.jackson.annotation.JsonProperty("TCP")
        TCP("TCP"), @com.fasterxml.jackson.annotation.JsonProperty("UDP")
        UDP("UDP"), @com.fasterxml.jackson.annotation.JsonProperty("ICMP")
        ICMP("ICMP"), @com.fasterxml.jackson.annotation.JsonProperty("SCTP")
        SCTP("SCTP");

        java.lang.String value;

        Protocol(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    @com.fasterxml.jackson.annotation.JsonProperty("protocol")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Protocol protocol;

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }
}

