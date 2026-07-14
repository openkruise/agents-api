package io.openkruise.agents.client.v2.models.sandboxspec.lifecycle;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"exec","timeoutSeconds"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class PostUpgrade implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Exec specifies the command to execute inside the sandbox via envd.
     * The first element is the command, the rest are args.
     * For shell scripts, use: ["/bin/bash", "-c", "your-script"]
     */
    @com.fasterxml.jackson.annotation.JsonProperty("exec")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Exec specifies the command to execute inside the sandbox via envd.\nThe first element is the command, the rest are args.\nFor shell scripts, use: [\"/bin/bash\", \"-c\", \"your-script\"]")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxspec.lifecycle.postupgrade.Exec exec;

    public io.openkruise.agents.client.v2.models.sandboxspec.lifecycle.postupgrade.Exec getExec() {
        return exec;
    }

    public void setExec(io.openkruise.agents.client.v2.models.sandboxspec.lifecycle.postupgrade.Exec exec) {
        this.exec = exec;
    }

    /**
     * TimeoutSeconds is the timeout for the action execution in seconds.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("timeoutSeconds")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TimeoutSeconds is the timeout for the action execution in seconds.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Integer timeoutSeconds = 60;

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}

