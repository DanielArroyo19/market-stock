package com.market.stock.api;

import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import static com.market.stock.proto.QuoteMessage.Quote;

@Deprecated
public class StockPricesCollectorTransform implements Transformer<String, Quote, Quote> {

    private ProcessorContext context;
    private KeyValueStore<String, Quote> valueStore;

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        this.valueStore = (KeyValueStore<String, Quote>) context.getStateStore("value-store");
    }

    @Override
    public Quote transform(String key, Quote value) {
        Quote lastValue = valueStore.get(key);
        if (lastValue == null || Double.compare(lastValue.getLast(), value.getLast()) != 0) {
            // Value has changed, process it
            valueStore.put(key, value);
            return value;
        } else {
            // Value hasn't changed, skip processing
            return null;
        }
    }


    @Override
    public void close() {

    }
}
