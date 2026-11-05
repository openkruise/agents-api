package io.openkruise.agents.client.v2.models;

@io.fabric8.kubernetes.model.annotation.Version(value = "v1alpha1" , storage = true , served = true)
@io.fabric8.kubernetes.model.annotation.Group("security.agents.kruise.io")
@io.fabric8.kubernetes.model.annotation.Singular("agentauthenticationconfig")
@io.fabric8.kubernetes.model.annotation.Plural("agentauthenticationconfigs")
public class AgentAuthenticationConfig extends io.fabric8.kubernetes.client.CustomResource<io.openkruise.agents.client.v2.models.AgentAuthenticationConfigSpec, io.openkruise.agents.client.v2.models.AgentAuthenticationConfigStatus> implements io.fabric8.kubernetes.api.model.Namespaced {
}

