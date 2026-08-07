package io.openkruise.agents.client.v2.models;

@io.fabric8.kubernetes.model.annotation.Version(value = "v1alpha1" , storage = true , served = true)
@io.fabric8.kubernetes.model.annotation.Group("agents.kruise.io")
@io.fabric8.kubernetes.model.annotation.Singular("globalsecurityprofile")
@io.fabric8.kubernetes.model.annotation.Plural("globalsecurityprofiles")
public class GlobalSecurityProfile extends io.fabric8.kubernetes.client.CustomResource<io.openkruise.agents.client.v2.models.GlobalSecurityProfileSpec, io.openkruise.agents.client.v2.models.GlobalSecurityProfileStatus> {
}

