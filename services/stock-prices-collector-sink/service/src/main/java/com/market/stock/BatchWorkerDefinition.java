package com.market.stock;

import com.market.stock.kafka.topic.Topic;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.Set;

public abstract class BatchWorkerDefinition<Key, Value> {
    protected abstract String baseGroupId();
    protected abstract Topic<Key, Value> topic();
    protected abstract Set<Class<?>> exceptionsToRetry();
    protected abstract void processBatch(ConsumerRecords<Key, Value> records);

    public String groupId(String kafkaEnvironmentPrefix) {
        return kafkaEnvironmentPrefix + baseGroupId();
    }
}
