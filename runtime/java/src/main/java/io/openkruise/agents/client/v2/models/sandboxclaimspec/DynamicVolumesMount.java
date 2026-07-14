package io.openkruise.agents.client.v2.models.sandboxclaimspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"mountID","mountPath","pvName","readOnly","subPath"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class DynamicVolumesMount implements io.fabric8.kubernetes.api.model.KubernetesResource {

    @com.fasterxml.jackson.annotation.JsonProperty("mountID")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String mountID;

    public String getMountID() {
        return mountID;
    }

    public void setMountID(String mountID) {
        this.mountID = mountID;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("mountPath")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String mountPath;

    public String getMountPath() {
        return mountPath;
    }

    public void setMountPath(String mountPath) {
        this.mountPath = mountPath;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("pvName")
    @io.fabric8.generator.annotation.Required()
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String pvName;

    public String getPvName() {
        return pvName;
    }

    public void setPvName(String pvName) {
        this.pvName = pvName;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("readOnly")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Boolean readOnly;

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("subPath")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String subPath;

    public String getSubPath() {
        return subPath;
    }

    public void setSubPath(String subPath) {
        this.subPath = subPath;
    }
}

