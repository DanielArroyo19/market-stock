package com.market.stock.api;

import com.market.stock.proto.QuoteMessage;
import com.market.stock.serdes.QuoteValueSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.UUID;

import static com.market.stock.proto.QuoteMessage.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class StockPricesCollectorProcessTest {

    private TopologyTestDriver testDriver;
    
    private TestInputTopic<String, Quote> inputTopic;
    private TestOutputTopic<String, Quote> outputTopic;

    @BeforeEach
    public void setUp() {
        // Setup Kafka Streams properties
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-app-" + UUID.randomUUID());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234"); // Doesn't matter for testing
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        // Create the topology and build the test driver
        StockPricesCollectorProcess stockPricesCollectorProcess = new StockPricesCollectorProcess();
        Topology topology = stockPricesCollectorProcess.buildTopology();
        testDriver = new TopologyTestDriver(topology, props);

        // Set up input and output topics
        inputTopic = testDriver.createInputTopic("market.quotes.price",
                Serdes.String().serializer(), new QuoteValueSerde().serializer());
        outputTopic = testDriver.createOutputTopic("market.stock.price.last",
                Serdes.String().deserializer(), new QuoteValueSerde().deserializer());
    }
    @Test
    public void testStockPricesCollectorProcess() {
        // Send input data to the input topic
        Quote expected = Quote.newBuilder().setSymbol(Quote.Symbol.newBuilder().setSymbol("AAL").build()).setLast(4.0).build();
        inputTopic.pipeInput("AAL", expected);

        // Read and verify output from the output topic
        KeyValue<String, Quote> result = outputTopic.readKeyValue();
        System.out.println("Aqui deberia" + result.value);
        assertThat(result).isNotNull();
        assertThat(expected).isEqualTo(result.value);
        // Add your assertions based on the processing logic in StockPricesCollectorProcess
    }

    @Test
    public void testStockPricesCollectorProcessFilterPrev() {
        // Send input data to the input topic
        Quote expected = Quote.newBuilder().setSymbol(Quote.Symbol.newBuilder().setSymbol("AAL").build()).setLast(5.0).build();
        Quote notExpected = Quote.newBuilder().setSymbol(Quote.Symbol.newBuilder().setSymbol("AAL").build()).setLast(4.0).build();
        inputTopic.pipeInput("AAL", notExpected);
        inputTopic.pipeInput("AAL", notExpected);
        inputTopic.pipeInput("AAL", expected);

        // Read and verify output from the output topic
        var result = outputTopic.readKeyValuesToList();
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        // Add your assertions based on the processing logic in StockPricesCollectorProcess
    }

    @Test
    public void testStockPricesCollectorProcessNoFilterPrev() {
        // Send input data to the input topic
        Quote expected = Quote.newBuilder().setSymbol(Quote.Symbol.newBuilder().setSymbol("AAL").build()).setLast(5.0).build();
        Quote notExpected = Quote.newBuilder().setSymbol(Quote.Symbol.newBuilder().setSymbol("AAL").build()).setLast(4.0).build();
        Quote notExpected2 = Quote.newBuilder().setSymbol(Quote.Symbol.newBuilder().setSymbol("AAL").build()).setLast(3.0).build();

        inputTopic.pipeInput("AAL", notExpected);
        inputTopic.pipeInput("AAL", notExpected2);
        inputTopic.pipeInput("AAL", expected);

        // Read and verify output from the output topic
        var result = outputTopic.readKeyValuesToList();
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(3);
        // Add your assertions based on the processing logic in StockPricesCollectorProcess
    }
}