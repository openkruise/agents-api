package io.openkruise.agents.client.v2.models.sandboxsetspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({"hibernateStrategy","type"})
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class PauseStrategy implements io.fabric8.kubernetes.api.model.KubernetesResource {

    /**
     * HibernateStrategy configures the storage medium for hibernate state.
     * Only effective when Type is Hibernate.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("hibernateStrategy")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("HibernateStrategy configures the storage medium for hibernate state.\nOnly effective when Type is Hibernate.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private io.openkruise.agents.client.v2.models.sandboxsetspec.pausestrategy.HibernateStrategy hibernateStrategy;

    public io.openkruise.agents.client.v2.models.sandboxsetspec.pausestrategy.HibernateStrategy getHibernateStrategy() {
        return hibernateStrategy;
    }

    public void setHibernateStrategy(io.openkruise.agents.client.v2.models.sandboxsetspec.pausestrategy.HibernateStrategy hibernateStrategy) {
        this.hibernateStrategy = hibernateStrategy;
    }

    public enum Type {

        @com.fasterxml.jackson.annotation.JsonProperty("Stop")
        STOP("Stop"), @com.fasterxml.jackson.annotation.JsonProperty("Hibernate")
        HIBERNATE("Hibernate");

        java.lang.String value;

        Type(java.lang.String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue()
        public java.lang.String getValue() {
            return value;
        }
    }

    /**
     * Type selects the pause mechanism.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("type")
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Type selects the pause mechanism.")
    @com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
    private Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}

