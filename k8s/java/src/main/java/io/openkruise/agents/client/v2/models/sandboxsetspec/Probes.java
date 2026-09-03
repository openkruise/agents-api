package io.openkruise.agents.client.v2.models.sandboxsetspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"containerName","exec","failureThreshold","grpc","httpGet","initialDelaySeconds","name","periodSeconds","successThreshold","tcpSocket","terminationGracePeriodSeconds","timeoutSeconds"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Probes implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * ContainerName specifies which container to execute the probe in.
     * If empty, defaults to the first container in the pod spec.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("containerName")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ContainerName specifies which container to execute the probe in.\nIf empty, defaults to the first container in the pod spec.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String containerName;

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    /**
     * Exec specifies a command to execute in the container.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("exec")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Exec specifies a command to execute in the container.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxsetspec.probes.Exec exec;

    public io.openkruise.agents.client.v2.models.sandboxsetspec.probes.Exec getExec() {
        return exec;
    }

    public void setExec(io.openkruise.agents.client.v2.models.sandboxsetspec.probes.Exec exec) {
        this.exec = exec;
    }

    /**
     * Minimum consecutive failures for the probe to be considered failed after having succeeded.
     * Defaults to 3. Minimum value is 1.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("failureThreshold")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Minimum consecutive failures for the probe to be considered failed after having succeeded.\nDefaults to 3. Minimum value is 1.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer failureThreshold;

    public Integer getFailureThreshold() {
        return failureThreshold;
    }

    public void setFailureThreshold(Integer failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    /**
     * GRPC specifies a GRPC HealthCheckRequest.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("grpc")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("GRPC specifies a GRPC HealthCheckRequest.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxsetspec.probes.Grpc grpc;

    public io.openkruise.agents.client.v2.models.sandboxsetspec.probes.Grpc getGrpc() {
        return grpc;
    }

    public void setGrpc(io.openkruise.agents.client.v2.models.sandboxsetspec.probes.Grpc grpc) {
        this.grpc = grpc;
    }

    /**
     * HTTPGet specifies an HTTP GET request to perform.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("httpGet")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("HTTPGet specifies an HTTP GET request to perform.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxsetspec.probes.HttpGet httpGet;

    public io.openkruise.agents.client.v2.models.sandboxsetspec.probes.HttpGet getHttpGet() {
        return httpGet;
    }

    public void setHttpGet(io.openkruise.agents.client.v2.models.sandboxsetspec.probes.HttpGet httpGet) {
        this.httpGet = httpGet;
    }

    /**
     * Number of seconds after the container has started before liveness probes are initiated.
     * More info: https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle#container-probes
     */
    @com.fasterxml.jackson.annotation.JsonProperty("initialDelaySeconds")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Number of seconds after the container has started before liveness probes are initiated.\nMore info: https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle#container-probes")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer initialDelaySeconds;

    public Integer getInitialDelaySeconds() {
        return initialDelaySeconds;
    }

    public void setInitialDelaySeconds(Integer initialDelaySeconds) {
        this.initialDelaySeconds = initialDelaySeconds;
    }

    /**
     * Name is the unique identifier for this probe within the sandbox.
     * Probe results are written to a Condition with type "agents.kruise.io/<Name>".
     */
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    @io.fabric8.generator.annotation.Required()
    @io.fabric8.generator.annotation.Pattern("^[A-Za-z0-9]([-A-Za-z0-9_.]*[A-Za-z0-9])?$")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Name is the unique identifier for this probe within the sandbox.\nProbe results are written to a Condition with type \"agents.kruise.io/<Name>\".")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * How often (in seconds) to perform the probe.
     * Default to 10 seconds. Minimum value is 1.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("periodSeconds")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("How often (in seconds) to perform the probe.\nDefault to 10 seconds. Minimum value is 1.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer periodSeconds;

    public Integer getPeriodSeconds() {
        return periodSeconds;
    }

    public void setPeriodSeconds(Integer periodSeconds) {
        this.periodSeconds = periodSeconds;
    }

    /**
     * Minimum consecutive successes for the probe to be considered successful after having failed.
     * Defaults to 1. Must be 1 for liveness and startup. Minimum value is 1.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("successThreshold")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Minimum consecutive successes for the probe to be considered successful after having failed.\nDefaults to 1. Must be 1 for liveness and startup. Minimum value is 1.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer successThreshold;

    public Integer getSuccessThreshold() {
        return successThreshold;
    }

    public void setSuccessThreshold(Integer successThreshold) {
        this.successThreshold = successThreshold;
    }

    /**
     * TCPSocket specifies a connection to a TCP port.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("tcpSocket")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TCPSocket specifies a connection to a TCP port.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxsetspec.probes.TcpSocket tcpSocket;

    public io.openkruise.agents.client.v2.models.sandboxsetspec.probes.TcpSocket getTcpSocket() {
        return tcpSocket;
    }

    public void setTcpSocket(io.openkruise.agents.client.v2.models.sandboxsetspec.probes.TcpSocket tcpSocket) {
        this.tcpSocket = tcpSocket;
    }

    /**
     * Optional duration in seconds the pod needs to terminate gracefully upon probe failure.
     * The grace period is the duration in seconds after the processes running in the pod are sent
     * a termination signal and the time when the processes are forcibly halted with a kill signal.
     * Set this value longer than the expected cleanup time for your process.
     * If this value is nil, the pod's terminationGracePeriodSeconds will be used. Otherwise, this
     * value overrides the value provided by the pod spec.
     * Value must be non-negative integer. The value zero indicates stop immediately via
     * the kill signal (no opportunity to shut down).
     * This is a beta field and requires enabling ProbeTerminationGracePeriod feature gate.
     * Minimum value is 1. spec.terminationGracePeriodSeconds is used if unset.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("terminationGracePeriodSeconds")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Optional duration in seconds the pod needs to terminate gracefully upon probe failure.\nThe grace period is the duration in seconds after the processes running in the pod are sent\na termination signal and the time when the processes are forcibly halted with a kill signal.\nSet this value longer than the expected cleanup time for your process.\nIf this value is nil, the pod's terminationGracePeriodSeconds will be used. Otherwise, this\nvalue overrides the value provided by the pod spec.\nValue must be non-negative integer. The value zero indicates stop immediately via\nthe kill signal (no opportunity to shut down).\nThis is a beta field and requires enabling ProbeTerminationGracePeriod feature gate.\nMinimum value is 1. spec.terminationGracePeriodSeconds is used if unset.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Long terminationGracePeriodSeconds;

    public Long getTerminationGracePeriodSeconds() {
        return terminationGracePeriodSeconds;
    }

    public void setTerminationGracePeriodSeconds(Long terminationGracePeriodSeconds) {
        this.terminationGracePeriodSeconds = terminationGracePeriodSeconds;
    }

    /**
     * Number of seconds after which the probe times out.
     * Defaults to 1 second. Minimum value is 1.
     * More info: https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle#container-probes
     */
    @com.fasterxml.jackson.annotation.JsonProperty("timeoutSeconds")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Number of seconds after which the probe times out.\nDefaults to 1 second. Minimum value is 1.\nMore info: https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle#container-probes")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer timeoutSeconds;

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}

