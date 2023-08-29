package com.market.stock;

import com.market.stock.kafka.topic.Topic;
import org.apache.kafka.streams.state.StoreBuilder;

import java.util.List;

import static java.util.Collections.emptyList;

public abstract class StreamProcessorDefinition {
    public static final String OVERRIDE_PROPERTIES_SEPARATOR = ";";
    public static final String OVERRIDE_PROPERTIES_ASSIGNMENT = "=";

    protected abstract String baseApplicationId();

    public abstract List<Topic<?, ?>> topics();

    public abstract void topology(Streams streams);

    public List<StoreBuilder<?>> stateStores() {
        return emptyList();
    }

    /**
     * This method determines which Stream Properties will be used,
     * by default checks if any statestore was included in the stateStores() method.
     * Override to true if the processor uses statestores created by the DSL API
     * and you need to increase the max time before triggering a rebalance
     * or you need to increase the buffer time before propagating aggregated changes downstream (30s)
     * such as the processors with large statestore size, multiple updates to the same aggregated record
     * or large processing time per record.
     */
    public boolean stateful() {
        return !stateStores().isEmpty();
    }

    public void precondition() {
    }

    public String applicationId(String kafkaEnvironmentPrefix) {
        return kafkaEnvironmentPrefix + baseApplicationId();
    }

}
