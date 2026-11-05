package io.openkruise.agents.client.v2.models.sandboxtemplatespec.buildoptions;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"command","cwd","env","user"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Steps implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Command is the shell command to execute.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("command")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Command is the shell command to execute.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String command;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * Cwd optionally selects the working directory inside the container.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("cwd")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Cwd optionally selects the working directory inside the container.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String cwd;

    public String getCwd() {
        return cwd;
    }

    public void setCwd(String cwd) {
        this.cwd = cwd;
    }

    /**
     * Env is the complete environment of this command.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("env")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Env is the complete environment of this command.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.Map<java.lang.String, String> env;

    public java.util.Map<java.lang.String, String> getEnv() {
        return env;
    }

    public void setEnv(java.util.Map<java.lang.String, String> env) {
        this.env = env;
    }

    /**
     * User optionally selects the executing user inside the container.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("user")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("User optionally selects the executing user inside the container.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}

