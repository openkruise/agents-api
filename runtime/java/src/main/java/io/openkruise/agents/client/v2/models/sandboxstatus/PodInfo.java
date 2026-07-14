package io.openkruise.agents.client.v2.models.sandboxstatus;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"annotations","labels","nodeName","podIP","podUID"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class PodInfo implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Annotations contains pod important annotations
     */
    @com.fasterxml.jackson.annotation.JsonProperty("annotations")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Annotations contains pod important annotations")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.Map<java.lang.String, String> annotations;

    public java.util.Map<java.lang.String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(java.util.Map<java.lang.String, String> annotations) {
        this.annotations = annotations;
    }

    /**
     * Labels contains pod important labels
     */
    @com.fasterxml.jackson.annotation.JsonProperty("labels")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Labels contains pod important labels")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private java.util.Map<java.lang.String, String> labels;

    public java.util.Map<java.lang.String, String> getLabels() {
        return labels;
    }

    public void setLabels(java.util.Map<java.lang.String, String> labels) {
        this.labels = labels;
    }

    /**
     * NodeName indicates in which node this pod is scheduled.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("nodeName")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("NodeName indicates in which node this pod is scheduled.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String nodeName;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * PodIP address allocated to the pod.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("podIP")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("PodIP address allocated to the pod.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String podIP;

    public String getPodIP() {
        return podIP;
    }

    public void setPodIP(String podIP) {
        this.podIP = podIP;
    }

    /**
     * PodUID is pod uid.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("podUID")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("PodUID is pod uid.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String podUID;

    public String getPodUID() {
        return podUID;
    }

    public void setPodUID(String podUID) {
        this.podUID = podUID;
    }
}

