package com.market.stock;

import com.market.stock.kafka.topic.Topic;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

public class Streams {
    private final String kafkaEnvironmentPrefix;
    private final StreamsBuilder streamsBuilder;

    public Streams(String kafkaEnvironmentPrefix, StreamsBuilder streamsBuilder) {
        this.kafkaEnvironmentPrefix = kafkaEnvironmentPrefix;
        this.streamsBuilder = streamsBuilder;
    }

    public <Key, Value> KStream<Key, Value> of(Topic<Key, Value> topic) {
        return streamsBuilder.stream(
                topic.name(kafkaEnvironmentPrefix), Consumed.with(topic.keySerde(), topic.valueSerde(), topic.timestampExtractor(), null)
        );
    }

    public <Key, Value> void to(Topic<Key, Value> topic, KStream<Key, Value> stream) {
        stream.to(topic.name(kafkaEnvironmentPrefix), Produced.with(topic.keySerde(), topic.valueSerde()));
    }
}