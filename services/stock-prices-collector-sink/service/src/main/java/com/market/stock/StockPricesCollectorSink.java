package com.market.stock;

import com.market.stock.persistence.dynamodb.DynamoDb;
import com.market.stock.proto.QuoteMessage;
import com.market.stock.repository.StockRepository;
import com.market.stock.sink.StockPricesCollectorWorkerDefinition;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static com.market.stock.kafka.topic.processor.collect.QuoteLastPriceTopic.QUOTE_LAST_PRICE_TOPIC;
import static com.market.stock.proto.QuoteMessage.*;
import static java.lang.System.*;

public class StockPricesCollectorSink {
    private final BatchWorker<String, Quote> stockPricesCollector;

    public StockPricesCollectorSink(BatchWorker<String, Quote> stockPricesCollector) {
        this.stockPricesCollector = stockPricesCollector;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        var processorConfiguration = new StreamProcessorConfiguration(
                getenv("KAFKA_STREAMS_GROUP_INSTANCE_ID"),
                getenv("KAFKA_BOOSTRAP_URL"),
                getenv("KAFKA_SECURITY_PROTOCOL"),
                getenv("KAFKA_ENVIRONMENT_PREFIX"),
                getenv("KAFKA_STATE_DIR"),
                getenv("CONFLUENT_CLOUD_API_KEY"),
                getenv("CONFLUENT_CLOUD_API_SECRET")
        );


        StockRepository stockRepository = new StockRepository(DynamoDb.client(System.getenv("DYNAMO_ENDPOINT")), "table");
        var stockPricesCollectorWorkerDefinition = new StockPricesCollectorWorkerDefinition(
                stockRepository, QUOTE_LAST_PRICE_TOPIC, Collections.emptySet()
        );

        new StockPricesCollectorSink(
                new BatchWorker<>(
                        stockPricesCollectorWorkerDefinition,
                        processorConfiguration,
                        BatchWorker.batchProperties(processorConfiguration, stockPricesCollectorWorkerDefinition)
                )
        ).start();
    }

    public void start() throws ExecutionException, InterruptedException {
        stockPricesCollector.start();
    }

    public void stop() throws InterruptedException {
        stockPricesCollector.terminate();
    }

    public boolean isStopped() {
        return !stockPricesCollector.isAlive();
    }
}
