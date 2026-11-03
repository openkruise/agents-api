package io.openkruise.agents.client.v2.models;

@io.fabric8.kubernetes.model.annotation.Version(value = "v1alpha1" , storage = true , served = true)
@io.fabric8.kubernetes.model.annotation.Group("security.agents.kruise.io")
@io.fabric8.kubernetes.model.annotation.Singular("agentrolebinding")
@io.fabric8.kubernetes.model.annotation.Plural("agentrolebindings")
public class AgentRoleBinding extends io.fabric8.kubernetes.client.CustomResource<io.openkruise.agents.client.v2.models.AgentRoleBindingSpec, io.openkruise.agents.client.v2.models.AgentRoleBindingStatus> implements io.fabric8.kubernetes.api.model.Namespaced {
}

