package io.openkruise.agents.client.v2.models.sandboxspec.probes;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"mode","port","service"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Grpc implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * mode specifies the connection mode for the gRPC health probe.
     * Set to "TLS" to use TLS without certificate verification.
     * Set to "Plaintext" to use a plaintext (insecure) connection explicitly.
     * If not specified, the probe uses a plaintext (insecure) connection.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("mode")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("mode specifies the connection mode for the gRPC health probe.\nSet to \"TLS\" to use TLS without certificate verification.\nSet to \"Plaintext\" to use a plaintext (insecure) connection explicitly.\nIf not specified, the probe uses a plaintext (insecure) connection.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String mode;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * Port number of the gRPC service. Number must be in the range 1 to 65535.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("port")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Port number of the gRPC service. Number must be in the range 1 to 65535.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer port;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Service is the name of the service to place in the gRPC HealthCheckRequest
     * (see https://github.com/grpc/grpc/blob/master/doc/health-checking.md).
     *
     * If this is not specified, the default behavior is defined by gRPC.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("service")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Service is the name of the service to place in the gRPC HealthCheckRequest\n(see https://github.com/grpc/grpc/blob/master/doc/health-checking.md).\n\nIf this is not specified, the default behavior is defined by gRPC.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String service = "";

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}

