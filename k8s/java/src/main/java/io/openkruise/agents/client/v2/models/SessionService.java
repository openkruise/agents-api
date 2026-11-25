package io.openkruise.agents.client.v2.models;

@io.fabric8.kubernetes.model.annotation.Version(value = "v1alpha1" , storage = true , served = true)
@io.fabric8.kubernetes.model.annotation.Group("agents.kruise.io")
@io.fabric8.kubernetes.model.annotation.Singular("sessionservice")
@io.fabric8.kubernetes.model.annotation.Plural("sessionservices")
public class SessionService extends io.fabric8.kubernetes.client.CustomResource<io.openkruise.agents.client.v2.models.SessionServiceSpec, io.openkruise.agents.client.v2.models.SessionServiceStatus> implements io.fabric8.kubernetes.api.model.Namespaced {
}

