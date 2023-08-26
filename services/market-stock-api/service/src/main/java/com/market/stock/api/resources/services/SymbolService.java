package com.market.stock.api.resources.services;

import com.market.stock.Stock;
import com.market.stock.api.resources.dto.Symbol;
import com.market.stock.repository.StockRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SymbolService {

    private StockRepository stockRepository;

    public SymbolService(StockRepository stockRepository){
        this.stockRepository = stockRepository;
    }

    public List<Symbol> getSymbol() {
        return mapStock(stockRepository.all());
    }

    public Symbol getSymbolById(String id) {
        return mapStock(Arrays.asList(stockRepository.get(id))).stream().findAny().get();
    }

    public void addSymbol(Symbol symbol) {
        Stock stockSave = Stock.builder().stock(symbol.getId()).build();
        stockRepository.save(null, stockSave);
    }

    private List<Symbol> mapStock (List<Stock> listStocks){
        return listStocks.stream().map(stock -> {
            return Symbol.builder()
                    .id(stock.getStock())
                    .build();
        }).collect(Collectors.toList());
    }
}
