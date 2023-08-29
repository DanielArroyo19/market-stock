package com.market.stock.kafka.topic;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.processor.TimestampExtractor;

import java.time.Duration;

public abstract class Topic<Key, Value> {
    private static final long ONE_WEEK_IN_DAYS = 31L;

    public abstract Serde<Key> keySerde();
    public abstract Serde<Value> valueSerde();
    public abstract Integer numberOfPartitions();
    public abstract String baseName();

    public String name(String kafkaEnvironmentPrefix) {
        return kafkaEnvironmentPrefix + baseName();
    }


    public long retentionInMilliseconds() { return Duration.ofDays(ONE_WEEK_IN_DAYS).getSeconds() * 1000; }

    public TimestampExtractor timestampExtractor() {
        return null;
    }
}
