package io.openkruise.agents.client.v2.models.trafficpolicyspec.ingress.rules;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"cidr","fqdn","service","workload"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class From implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * CIDR is an IP address range in CIDR notation (e.g. "10.0.0.0/8").
     */
    @com.fasterxml.jackson.annotation.JsonProperty("cidr")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("CIDR is an IP address range in CIDR notation (e.g. \"10.0.0.0/8\").")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String cidr;

    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }

    /**
     * FQDN is a fully qualified domain name to match (e.g. "api.example.com").
     */
    @com.fasterxml.jackson.annotation.JsonProperty("fqdn")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("FQDN is a fully qualified domain name to match (e.g. \"api.example.com\").")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String fqdn;

    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String fqdn) {
        this.fqdn = fqdn;
    }

    /**
     * Service references a Kubernetes Service and its selected endpoints.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("service")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Service references a Kubernetes Service and its selected endpoints.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.trafficpolicyspec.ingress.rules.from.Service service;

    public io.openkruise.agents.client.v2.models.trafficpolicyspec.ingress.rules.from.Service getService() {
        return service;
    }

    public void setService(io.openkruise.agents.client.v2.models.trafficpolicyspec.ingress.rules.from.Service service) {
        this.service = service;
    }

    /**
     * Workload selects pods by namespace and labels; their IP addresses form
     * the peer address set.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("workload")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Workload selects pods by namespace and labels; their IP addresses form\nthe peer address set.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.trafficpolicyspec.ingress.rules.from.Workload workload;

    public io.openkruise.agents.client.v2.models.trafficpolicyspec.ingress.rules.from.Workload getWorkload() {
        return workload;
    }

    public void setWorkload(io.openkruise.agents.client.v2.models.trafficpolicyspec.ingress.rules.from.Workload workload) {
        this.workload = workload;
    }
}

