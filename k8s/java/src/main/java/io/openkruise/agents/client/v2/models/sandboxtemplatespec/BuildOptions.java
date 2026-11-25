package io.openkruise.agents.client.v2.models.sandboxtemplatespec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"checkpoint","fromTemplate","imageAcceleration","readyCommand","startCommand","steps"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class BuildOptions implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Checkpoint captures the completed build as a restorable artifact.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("checkpoint")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Checkpoint captures the completed build as a restorable artifact.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxtemplatespec.buildoptions.Checkpoint checkpoint;

    public io.openkruise.agents.client.v2.models.sandboxtemplatespec.buildoptions.Checkpoint getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(io.openkruise.agents.client.v2.models.sandboxtemplatespec.buildoptions.Checkpoint checkpoint) {
        this.checkpoint = checkpoint;
    }

    /**
     * FromTemplate names an existing logical Template used as the build base.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("fromTemplate")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("FromTemplate names an existing logical Template used as the build base.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String fromTemplate;

    public String getFromTemplate() {
        return fromTemplate;
    }

    public void setFromTemplate(String fromTemplate) {
        this.fromTemplate = fromTemplate;
    }

    /**
     * ImageAcceleration requests best-effort image pre-warming.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("imageAcceleration")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ImageAcceleration requests best-effort image pre-warming.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxtemplatespec.buildoptions.ImageAcceleration imageAcceleration;

    public io.openkruise.agents.client.v2.models.sandboxtemplatespec.buildoptions.ImageAcceleration getImageAcceleration() {
        return imageAcceleration;
    }

    public void setImageAcceleration(io.openkruise.agents.client.v2.models.sandboxtemplatespec.buildoptions.ImageAcceleration imageAcceleration) {
        this.imageAcceleration = imageAcceleration;
    }

    /**
     * ReadyCommand checks business process readiness before capture.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("readyCommand")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("ReadyCommand checks business process readiness before capture.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxtemplatespec.buildoptions.ReadyCommand readyCommand;

    public io.openkruise.agents.client.v2.models.sandboxtemplatespec.buildoptions.ReadyCommand getReadyCommand() {
        return readyCommand;
    }

    public void setReadyCommand(io.openkruise.agents.client.v2.models.sandboxtemplatespec.buildoptions.ReadyCommand readyCommand) {
        this.readyCommand = readyCommand;
    }

    /**
     * StartCommand starts the business process before capture.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("startCommand")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("StartCommand starts the business process before capture.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxtemplatespec.buildoptions.StartCommand startCommand;

    public io.openkruise.agents.client.v2.models.sandboxtemplatespec.buildoptions.StartCommand getStartCommand() {
        return startCommand;
    }

    public void setStartCommand(io.openkruise.agents.client.v2.models.sandboxtemplatespec.buildoptions.StartCommand startCommand) {
        this.startCommand = startCommand;
    }

    /**
     * Steps is the ordered provisioning sequence.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("steps")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Steps is the ordered provisioning sequence.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.List<io.openkruise.agents.client.v2.models.sandboxtemplatespec.buildoptions.Steps> steps;

    public java.util.List<io.openkruise.agents.client.v2.models.sandboxtemplatespec.buildoptions.Steps> getSteps() {
        return steps;
    }

    public void setSteps(java.util.List<io.openkruise.agents.client.v2.models.sandboxtemplatespec.buildoptions.Steps> steps) {
        this.steps = steps;
    }
}

