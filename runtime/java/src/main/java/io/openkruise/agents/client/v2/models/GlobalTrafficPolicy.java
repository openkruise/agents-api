package io.openkruise.agents.client.v2.models;

@io.fabric8.kubernetes.model.annotation.Version(value = "v1alpha1" , storage = true , served = true)
@io.fabric8.kubernetes.model.annotation.Group("agents.kruise.io")
@io.fabric8.kubernetes.model.annotation.Singular("globaltrafficpolicy")
@io.fabric8.kubernetes.model.annotation.Plural("globaltrafficpolicies")
public class GlobalTrafficPolicy extends io.fabric8.kubernetes.client.CustomResource<io.openkruise.agents.client.v2.models.GlobalTrafficPolicySpec, io.openkruise.agents.client.v2.models.GlobalTrafficPolicyStatus> {
}

