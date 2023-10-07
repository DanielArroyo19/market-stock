package com.market.stock;

import com.market.stock.persistence.dynamodb.DynamoDb;
import com.market.stock.proto.QuoteMessage;
import com.market.stock.repository.StockRepository;
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

import java.util.Map;
import java.util.Properties;

import static com.market.stock.repository.StockTable.Attributes.PRICE;

@Slf4j
public class StockPricesCollectorProcessorSink {

    private StockRepository stockRepository;

    public StockPricesCollectorProcessorSink(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public static void main (String[] main) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "stock-prices-collector-processor-sink");
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

        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100);


        var stockRepository = new StockRepository(DynamoDb.client(System.getenv("DYNAMO_ENDPOINT")), "Stock");

        var stockPricesCollectorProcessSink
                = new StockPricesCollectorProcessorSink(stockRepository);
        Topology topology = stockPricesCollectorProcessSink.buildTopology();

        final KafkaStreams streams = new KafkaStreams(topology, props);

        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    public Topology buildTopology() {

        final StreamsBuilder builder = new StreamsBuilder();

        Serde<QuoteMessage.Quote> valueSerde = new QuoteValueSerde();

        KStream<String, QuoteMessage.Quote> inputTopic = builder.stream("market.stock.price.last", Consumed.with(Serdes.String(), valueSerde));

        inputTopic
            .foreach((key, value) -> {
                try {
                    stockRepository.update(key, Map.of(PRICE, value.getLast()));
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Error updating stock repository: {}", e.getMessage());
                }
            });
        return builder.build();
    }
}
