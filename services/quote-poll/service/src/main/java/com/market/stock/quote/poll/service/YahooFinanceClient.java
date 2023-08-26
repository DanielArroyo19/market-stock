package com.market.stock.quote.poll.service;

import com.market.stock.quote.poll.dto.Result;
import com.market.stock.quote.poll.dto.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class YahooFinanceClient {
    @Value("${finance.yahoo.url}")
    private String financeYahooUrl;

    @Value("${finance.yahoo.all.url}")
    private String financeYahooAllUrl;

    @Autowired
    private RestTemplate restTemplate;

    public Result getStockById(String id){
        Root  quoteResponseEntity = restTemplate.getForObject(financeYahooUrl + id, Root.class);
        return quoteResponseEntity.getQuoteResponse().getResult().stream().findAny().orElseThrow(() -> new NoSuchElementException("Quote not found"));
    }

    public List<Result> getStocks(){
        Root  quoteResponseEntity = restTemplate.getForObject(financeYahooAllUrl, Root.class);
        return quoteResponseEntity.getQuoteResponse().getResult();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
