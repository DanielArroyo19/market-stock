package com.market.stock.quote.poll.configuration;

import com.market.stock.persistence.dynamodb.DynamoDb;
import com.market.stock.repository.StockRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StockRepositoryConfiguration {

    private final String dynamoDBUrl;
    private final String table;

    public StockRepositoryConfiguration(@Value("${dynamo.url}") String dynamoDBUrl,
                                        @Value("${dynamo.table}") String table){
        this.dynamoDBUrl = dynamoDBUrl;
        this.table = table;
    }

    @Bean
    public StockRepository stockRepository() {
        return new StockRepository(
                DynamoDb.client(dynamoDBUrl), table);
    }
}
