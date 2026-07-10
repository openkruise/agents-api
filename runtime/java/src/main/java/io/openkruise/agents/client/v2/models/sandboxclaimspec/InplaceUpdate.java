package io.openkruise.agents.client.v2.models.sandboxclaimspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"image","resources"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class InplaceUpdate implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * Image specifies the new image to update to.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("image")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Image specifies the new image to update to.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private String image;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Resources specifies in-place resource update options.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("resources")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Resources specifies in-place resource update options.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxclaimspec.inplaceupdate.Resources resources;

    public io.openkruise.agents.client.v2.models.sandboxclaimspec.inplaceupdate.Resources getResources() {
        return resources;
    }

    public void setResources(io.openkruise.agents.client.v2.models.sandboxclaimspec.inplaceupdate.Resources resources) {
        this.resources = resources;
    }
}

