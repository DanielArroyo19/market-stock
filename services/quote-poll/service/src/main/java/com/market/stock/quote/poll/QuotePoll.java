package com.market.stock.quote.poll;

import com.google.protobuf.Timestamp;
import com.market.stock.proto.Quotes.Quote;
import com.market.stock.quote.poll.dto.Result;
import com.market.stock.quote.poll.service.TopicProducer;
import com.market.stock.quote.poll.service.YahooFinanceClient;
import com.market.stock.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@EnableKafka
@SpringBootApplication
public class QuotePoll implements CommandLineRunner {

    @Autowired private YahooFinanceClient yahooFinanceClient;
    @Autowired private TopicProducer topicProducer;

    public static void main(String[] args) {
        SpringApplication.run(QuotePoll.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        /*var stockList = stockRepository.all().stream()
                .filter(stock -> Optional.of(stock.getEnabled()).isPresent())
                .collect(Collectors.toList());*/
        List<Result> results = yahooFinanceClient.getStocks();
        results.stream().forEach(result -> {
            Quote quote = Quote.newBuilder()
                    .setCurrency(Quote.Currency.USD)
                    .setAsk(result.getAsk())
                    .setBid(result.getBid())
                    .setLast(result.getRegularMarketPrice())
                    .setSymbol(Quote.Symbol.newBuilder().setSymbol(result.getSymbol()).build())
                    .setTransactionTimestamp(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                    .build();

            topicProducer.send(TopicProducer.TOPIC, quote.getSymbol().getSymbol(), quote);
        });
    }

}
