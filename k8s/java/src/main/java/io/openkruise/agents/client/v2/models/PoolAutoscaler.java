package io.openkruise.agents.client.v2.models;

@io.fabric8.kubernetes.model.annotation.Version(value = "v1alpha1" , storage = true , served = true)
@io.fabric8.kubernetes.model.annotation.Group("agents.kruise.io")
@io.fabric8.kubernetes.model.annotation.Singular("poolautoscaler")
@io.fabric8.kubernetes.model.annotation.Plural("poolautoscalers")
public class PoolAutoscaler extends io.fabric8.kubernetes.client.CustomResource<io.openkruise.agents.client.v2.models.PoolAutoscalerSpec, io.openkruise.agents.client.v2.models.PoolAutoscalerStatus> implements io.fabric8.kubernetes.api.model.Namespaced {
}

