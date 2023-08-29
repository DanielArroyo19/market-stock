package com.market.stock;

import com.market.stock.kafka.topic.Topic;
import com.market.stock.kafka.topic.processor.collect.QuoteLastPriceTopic;
import com.market.stock.proto.QuoteMessage;
import com.market.stock.repository.StockRepository;
import com.market.stock.serdes.QuoteValueSerde;
import com.market.stock.sink.StockPricesCollectorWorkerDefinition;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.market.stock.kafka.topic.processor.collect.QuoteLastPriceTopic.*;
import static com.market.stock.proto.QuoteMessage.*;
import static java.lang.System.getenv;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.openMocks;

public class StockPricesCollectorSinkTest {

    private TestInputTopic<String, Quote> inputTopic;

    private StockRepository stockRepository;

    private StockPricesCollectorSink stockPricesCollectorSink;

    private TopologyTestDriver testDriver;



    @BeforeEach
    public void setUp() throws ExecutionException, InterruptedException {
        openMocks(this);

        /*var processorConfiguration = new StreamProcessorConfiguration(
                getenv("KAFKA_STREAMS_GROUP_INSTANCE_ID"),
                getenv("KAFKA_BOOSTRAP_URL"),
                getenv("KAFKA_SECURITY_PROTOCOL"),
                getenv("KAFKA_ENVIRONMENT_PREFIX"),
                getenv("KAFKA_STATE_DIR"),
                getenv("CONFLUENT_CLOUD_API_KEY"),
                getenv("CONFLUENT_CLOUD_API_SECRET")
        );*/

        var processorConfiguration = new StreamProcessorConfiguration(
                "test-consumer-group-sink",
                "dummy:1234",
                "test-",
                "/temp",
                "PLAIN",
                "",
                ""
        );

        // Create your Kafka Streams topology and build the test driver
        Topology topology = new StreamsBuilder().build(); // Replace with your actual topology
        testDriver = new TopologyTestDriver(topology, processorConfiguration.getProperties());

        inputTopic = testDriver.createInputTopic("market.stock.price.last",
                Serdes.String().serializer(), new QuoteValueSerde().serializer());

        stockRepository = mock(StockRepository.class);

        var stockPricesCollectorWorkerDefinition = new StockPricesCollectorWorkerDefinition(
                stockRepository,
                QUOTE_LAST_PRICE_TOPIC,
                Collections.emptySet()
        );

        stockPricesCollectorSink = new StockPricesCollectorSink(
                new BatchWorker<>(
                        stockPricesCollectorWorkerDefinition,
                        processorConfiguration,
                        BatchWorker.batchProperties(processorConfiguration, stockPricesCollectorWorkerDefinition)
                )
        );

        stockPricesCollectorSink.start();
    }

    @Test
    public void testKafkaToDynamoDB() {
        //doNothing().when(stockRepository).save(null, any(Stock.class));

        emitQuote("AAL", 12.0);

        //await().atMost(Duration.ofSeconds(AWAIT_SECONDS)).until(() -> allReadings(siteMeterAggregateArgumentCaptor).size() == 2);

    }

    private void emitQuote(String stock, Double last){
        ProducerRecord<String, Quote> record = new ProducerRecord<String, Quote>(
                QUOTE_LAST_PRICE_TOPIC.baseName(),
                stock,
                Quote.newBuilder().setLast(last).build()
        );
        inputTopic.pipeInput(stock,
                Quote.newBuilder().setLast(last).build());
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        stockPricesCollectorSink.stop();
        assertTrue(stockPricesCollectorSink.isStopped());
    }
}
