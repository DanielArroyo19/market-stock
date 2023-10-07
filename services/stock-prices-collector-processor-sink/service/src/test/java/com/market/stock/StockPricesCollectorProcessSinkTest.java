package com.market.stock;

import com.market.stock.repository.StockRepository;
import com.market.stock.serdes.QuoteValueSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static com.market.stock.proto.QuoteMessage.Quote;
import static com.market.stock.repository.StockTable.Attributes.PRICE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class StockPricesCollectorProcessSinkTest {

    private TestInputTopic<String, Quote> inputTopic;
    private TopologyTestDriver testDriver;

    private StockRepository stockRepository;


    @BeforeEach
    public void setUp() {
        openMocks(this);

        stockRepository = mock(StockRepository.class);

        // Setup Kafka Streams properties
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-app-" + UUID.randomUUID());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234"); // Doesn't matter for testing
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Create the topology and build the test driver
        StockPricesCollectorProcessorSink stockPricesCollectorProcess = new StockPricesCollectorProcessorSink(stockRepository);
        Topology topology = stockPricesCollectorProcess.buildTopology();
        testDriver = new TopologyTestDriver(topology, props);

        // Set up input and output topics
        inputTopic = testDriver.createInputTopic("market.stock.price.last",
                Serdes.String().serializer(), new QuoteValueSerde().serializer());
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    public void testStockPricesCollectorProcess() {
        ArgumentCaptor<Map> quoteArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> keyArgumentCaptor = ArgumentCaptor.forClass(String.class);

        //doNothing().when(stockRepository).update(keyArgumentCaptor.capture(), quoteArgumentCaptor.capture());

        // Send input data to the input topic
        Quote expected = Quote.newBuilder().setSymbol(Quote.Symbol.newBuilder().setSymbol("AAL").build()).setLast(4.0).build();
        inputTopic.pipeInput("AAL", expected);

        verify(stockRepository).update(keyArgumentCaptor.capture(), quoteArgumentCaptor.capture());

        assertThat(expected.getSymbol().getSymbol()).isEqualTo(keyArgumentCaptor.getValue());
        assertThat(Map.of(PRICE, expected.getLast())).isEqualTo(quoteArgumentCaptor.getValue());

    }

    @Test
    public void testStockPricesCollectorProcessError() {
        doThrow(RuntimeException.class).when(stockRepository).update(any(), anyMap());
        //doNothing().when(stockRepository).update(keyArgumentCaptor.capture(), quoteArgumentCaptor.capture());

        // Send input data to the input topic
        Quote expected = Quote.newBuilder().setSymbol(Quote.Symbol.newBuilder().setSymbol("AAL").build()).setLast(4.0).build();
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inputTopic.pipeInput("AAL", expected);
        });
    }

}