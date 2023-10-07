package com.market.stock.api;

import com.market.stock.serdes.QuoteValueSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

import java.util.Properties;

import static com.market.stock.proto.QuoteMessage.*;

@Slf4j
public class StockPricesCollectorProcess {

    private static final String STORE_NAME = "value-store";

    public static void main (String[] main) {

        /**************************************************
         * Build a Kafka Streams Topology
         **************************************************/
        //Setup Properties for the Kafka Input Stream
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "stock-prices-collector-process");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, System.getenv("KAFKA_BOOSTRAP_URL"));
        props.put(StreamsConfig.SECURITY_PROTOCOL_CONFIG, System.getenv("KAFKA_SECURITY_PROTOCOL"));
        if("SASL_SSL".equals(System.getenv("KAFKA_SECURITY_PROTOCOL"))){
            props.put("sasl.mechanism", System.getenv("KAFKA_SECURITY_MECHANISM"));
            props.put("sasl.jaas.config",
                    String.format(
                            "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";",
                    System.getenv("KAFKA_CLUSTER_API_KEY"),
                    System.getenv("KAFKA_CLUSTER_API_SECRET")));
        }
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, QuoteValueSerde.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"latest");

        //For immediate results during testing
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100);


        StockPricesCollectorProcess stockPricesCollectorProcess = new StockPricesCollectorProcess();
        Topology topology = stockPricesCollectorProcess.buildTopology();

        final KafkaStreams streams = new KafkaStreams(topology, props);

        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    public Topology buildTopology() {
        //Initiate the Kafka Streams Builder
        final StreamsBuilder builder = new StreamsBuilder();

        Serde<Quote> valueSerde = new QuoteValueSerde();

        KStream<String, Quote> inputTopic = builder.stream("market.quotes.price", Consumed.with(Serdes.String(), valueSerde));
        StoreBuilder<KeyValueStore<String, Quote>> storeBuilder = Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(STORE_NAME),
                Serdes.String(),
                valueSerde
        );
        builder.addStateStore(storeBuilder);


        inputTopic
                .process(new StockPricesCollectorProcessor(STORE_NAME), STORE_NAME)
                .filter((key, value) -> value != null)
                .to("market.stock.price.last", Produced.with(Serdes.String(), valueSerde));

        return builder.build();
    }
}
