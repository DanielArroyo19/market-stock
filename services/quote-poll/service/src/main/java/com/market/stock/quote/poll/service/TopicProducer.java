package com.market.stock.quote.poll.service;

import com.market.stock.proto.Quotes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TopicProducer {
    public static final String TOPIC = "market.quotes.price";

    private final KafkaTemplate<String, Quotes.Quote> kafkaTemplate;

    public void send(String topic, String key, Quotes.Quote value){
        kafkaTemplate.send(topic, key, value);
    }
}
