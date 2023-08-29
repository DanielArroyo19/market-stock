package com.market.stock.serdes;

import com.github.daniel.shuy.kafka.protobuf.serde.KafkaProtobufSerde;
import com.market.stock.proto.QuoteMessage.Quote;

public class QuoteValueSerde extends KafkaProtobufSerde<Quote> {
    public QuoteValueSerde() {
        super(Quote.parser());
    }
}
