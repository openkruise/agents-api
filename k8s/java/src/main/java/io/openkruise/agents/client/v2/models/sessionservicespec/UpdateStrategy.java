package io.openkruise.agents.client.v2.models.sessionservicespec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"maxSurge"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class UpdateStrategy implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * MaxSurge budgets extra members from observed active and terminating Sandbox
     * CRs during rolling replacement. Unknown create outcomes, stale caches, and
     * controller restarts can temporarily exceed it; it is not an instantaneous
     * limit and does not cap Pods still exiting after their CR disappears.
     * It must be positive even at zero replicas; whole percentages round up
     * against desired replicas. Zero is unsupported without maxUnavailable.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("maxSurge")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("MaxSurge budgets extra members from observed active and terminating Sandbox\nCRs during rolling replacement. Unknown create outcomes, stale caches, and\ncontroller restarts can temporarily exceed it; it is not an instantaneous\nlimit and does not cap Pods still exiting after their CR disappears.\nIt must be positive even at zero replicas; whole percentages round up\nagainst desired replicas. Zero is unsupported without maxUnavailable.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.fabric8.kubernetes.api.model.IntOrString maxSurge = new io.fabric8.kubernetes.api.model.IntOrString("25%");

    public io.fabric8.kubernetes.api.model.IntOrString getMaxSurge() {
        return maxSurge;
    }

    public void setMaxSurge(io.fabric8.kubernetes.api.model.IntOrString maxSurge) {
        this.maxSurge = maxSurge;
    }
}

