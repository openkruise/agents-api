package io.openkruise.agents.client.v2.models;

@io.fabric8.kubernetes.model.annotation.Version(value = "v1alpha1" , storage = true , served = true)
@io.fabric8.kubernetes.model.annotation.Group("agents.kruise.io")
@io.fabric8.kubernetes.model.annotation.Singular("checkpoint")
@io.fabric8.kubernetes.model.annotation.Plural("checkpoints")
public class Checkpoint extends io.fabric8.kubernetes.client.CustomResource<io.openkruise.agents.client.v2.models.CheckpointSpec, io.openkruise.agents.client.v2.models.CheckpointStatus> implements io.fabric8.kubernetes.api.model.Namespaced {
}

