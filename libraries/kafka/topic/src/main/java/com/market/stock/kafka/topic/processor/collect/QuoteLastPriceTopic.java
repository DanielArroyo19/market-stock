package com.market.stock.kafka.topic.processor.collect;

import com.market.stock.kafka.topic.Topic;
import com.market.stock.serdes.QuoteValueSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import static com.market.stock.proto.QuoteMessage.*;

public class QuoteLastPriceTopic extends Topic<String, Quote> {

    public static final QuoteLastPriceTopic QUOTE_LAST_PRICE_TOPIC = new QuoteLastPriceTopic();
    @Override
    public Serde<String> keySerde() {
        return new Serdes.StringSerde();
    }

    @Override
    public Serde<Quote> valueSerde() {
        return new QuoteValueSerde();
    }

    @Override
    public Integer numberOfPartitions() {
        return 2;
    }

    @Override
    public String baseName() {
        return "market.stock.price.last";
    }
}
