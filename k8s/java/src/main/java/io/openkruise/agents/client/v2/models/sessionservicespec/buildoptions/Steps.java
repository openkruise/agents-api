package io.openkruise.agents.client.v2.models.sessionservicespec.buildoptions;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"command","copy"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Steps implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * TemplateBuildCommand is one self-contained shell command in a Template
     * Build. User, Cwd, and Env are the complete execution context folded from
     * the caller's build DSL.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("command")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TemplateBuildCommand is one self-contained shell command in a Template\nBuild. User, Cwd, and Env are the complete execution context folded from\nthe caller's build DSL.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sessionservicespec.buildoptions.steps.Command command;

    public io.openkruise.agents.client.v2.models.sessionservicespec.buildoptions.steps.Command getCommand() {
        return command;
    }

    public void setCommand(io.openkruise.agents.client.v2.models.sessionservicespec.buildoptions.steps.Command command) {
        this.command = command;
    }

    /**
     * TemplateBuildCopy materializes an uploaded build-context file or directory.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("copy")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("TemplateBuildCopy materializes an uploaded build-context file or directory.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sessionservicespec.buildoptions.steps.Copy copy;

    public io.openkruise.agents.client.v2.models.sessionservicespec.buildoptions.steps.Copy getCopy() {
        return copy;
    }

    public void setCopy(io.openkruise.agents.client.v2.models.sessionservicespec.buildoptions.steps.Copy copy) {
        this.copy = copy;
    }
}

