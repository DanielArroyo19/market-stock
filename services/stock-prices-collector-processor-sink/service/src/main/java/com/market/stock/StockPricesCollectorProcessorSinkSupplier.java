package com.market.stock;

import com.market.stock.proto.QuoteMessage;
import com.market.stock.repository.StockRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.*;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Slf4j
public class StockPricesCollectorProcessorSinkSupplier implements FixedKeyProcessorSupplier<String, QuoteMessage.Quote, QuoteMessage.Quote> {


    private final StockRepository stockRepository;


    public StockPricesCollectorProcessorSinkSupplier(StockRepository stockRepository){
        this.stockRepository = stockRepository;
    }

    @Override
    public FixedKeyProcessor<String, QuoteMessage.Quote, QuoteMessage.Quote> get() {
        return new ValueChangeProcessor(stockRepository);
    }

    private static class ValueChangeProcessor implements FixedKeyProcessor<String, QuoteMessage.Quote, QuoteMessage.Quote> {

        private StockRepository stockRepository;

        public ValueChangeProcessor(StockRepository stockRepository) {
            this.stockRepository = stockRepository;
        }
        @Override
        public void process(FixedKeyRecord<String, QuoteMessage.Quote> record) {
            log.debug("record to sink {}", record.value());
            stockRepository.update(record.key(), Map.of("LAST", record.value().getLast()));
        }
    }
}
