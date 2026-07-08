package io.openkruise.agents.client.v2.models;

@io.fabric8.kubernetes.model.annotation.Version(value = "v1alpha1" , storage = true , served = true)
@io.fabric8.kubernetes.model.annotation.Group("agents.kruise.io")
@io.fabric8.kubernetes.model.annotation.Singular("commit")
@io.fabric8.kubernetes.model.annotation.Plural("commits")
public class Commit extends io.fabric8.kubernetes.client.CustomResource<io.openkruise.agents.client.v2.models.CommitSpec, io.openkruise.agents.client.v2.models.CommitStatus> implements io.fabric8.kubernetes.api.model.Namespaced {
}

