package com.market.stock.sink;

import com.market.stock.BatchWorkerDefinition;
import com.market.stock.kafka.topic.Topic;
import com.market.stock.proto.QuoteMessage;
import com.market.stock.repository.StockRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import static com.market.stock.proto.QuoteMessage.*;

@Slf4j
public class StockPricesCollectorWorkerDefinition extends BatchWorkerDefinition<String, Quote> {

    public static final String BASE_GROUP_ID = "stock-prices-collector-worker-definition";
    private final StockRepository stockRepository;
    private final Topic<String, Quote> stockPriceCollectorTopic;
    private final Set<Class<?>> exceptionsToRetry;

    public StockPricesCollectorWorkerDefinition(StockRepository stockRepository, Topic<String, Quote> stockPriceCollectorTopic, Set<Class<?>> exceptionsToRetry) {
        this.stockRepository = stockRepository;
        this.stockPriceCollectorTopic = stockPriceCollectorTopic;
        this.exceptionsToRetry = exceptionsToRetry;
    }

    @Override
    protected String baseGroupId() {
        return "stock-prices-collector-worker";
    }

    @Override
    protected Topic<String, Quote> topic() {
        return stockPriceCollectorTopic;
    }

    @Override
    protected Set<Class<?>> exceptionsToRetry() {
        return this.exceptionsToRetry;
    }

    @Override
    protected void processBatch(ConsumerRecords<String, Quote> records) {

        StreamSupport.stream(records.spliterator(), true).forEach( stock -> {
                log.debug("record to sink {}", stock);
                stockRepository.update(stock.key(), Map.of("LAST", stock.value().getLast()));
        });
    }
}
