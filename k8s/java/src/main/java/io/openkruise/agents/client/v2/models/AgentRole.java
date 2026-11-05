package io.openkruise.agents.client.v2.models;

@io.fabric8.kubernetes.model.annotation.Version(value = "v1alpha1" , storage = true , served = true)
@io.fabric8.kubernetes.model.annotation.Group("security.agents.kruise.io")
@io.fabric8.kubernetes.model.annotation.Singular("agentrole")
@io.fabric8.kubernetes.model.annotation.Plural("agentroles")
public class AgentRole extends io.fabric8.kubernetes.client.CustomResource<io.openkruise.agents.client.v2.models.AgentRoleSpec, io.openkruise.agents.client.v2.models.AgentRoleStatus> implements io.fabric8.kubernetes.api.model.Namespaced {
}

