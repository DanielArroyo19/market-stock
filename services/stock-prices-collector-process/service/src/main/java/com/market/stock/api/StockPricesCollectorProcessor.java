package com.market.stock.api;

import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.StateStore;
import org.apache.kafka.streams.processor.api.*;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;

import java.time.Duration;
import java.util.Set;

import static com.market.stock.proto.QuoteMessage.Quote;

public class StockPricesCollectorProcessor implements ProcessorSupplier<String, Quote, String, Quote> {

    private String stateStore;

    public StockPricesCollectorProcessor(String stateStore) {
        this.stateStore = stateStore;
    }

    @Override
    public Processor<String, Quote, String, Quote> get() {
        return new ValueChangeProcessor(stateStore);
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        return ProcessorSupplier.super.stores();
    }

    private static class ValueChangeProcessor extends ContextualProcessor<String, Quote, String, Quote> {
        private ProcessorContext<String, Quote> context;
        private KeyValueStore<String, Quote> store;

        private String stateStore;

        public ValueChangeProcessor(String stateStore) {
            this.stateStore = stateStore;
        }

        @Override
        public void init(ProcessorContext<String, Quote> context) {
            this.context = context;
            this.store = context.getStateStore(stateStore);
            // punctuate each second, can access this.state
            //context.schedule(Duration.ofSeconds(1), PunctuationType.WALL_CLOCK_TIME, new Punctuator(..));

        }

        @Override
        public void process(Record<String, Quote> record) {
            Quote quoteStored = store.get(record.key());
            if(quoteStored == null){
                quoteStored = Quote.newBuilder().setSymbol(Quote.Symbol.newBuilder().setSymbol(record.key()).build()).setLast(0.0).build();
            }
            if(Double.compare(quoteStored.getLast(), record.value().getLast()) != 0) {
                store.put(record.key(), record.value());
                context.forward(record);
            }
        }

        @Override
        public void close() {
            store.close();
        }
    }
}
