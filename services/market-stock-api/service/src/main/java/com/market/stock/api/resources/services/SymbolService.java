package com.market.stock.api.resources.services;

import com.market.stock.Stock;
import com.market.stock.api.resources.dto.Symbol;
import com.market.stock.repository.StockRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.market.stock.repository.StockTable.Attributes.PRICE;

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

    public void putLastPrice(String id, Symbol stock) {
        System.out.println("Aqui");
        stockRepository.update(id, Map.of(PRICE, stock.getLast()));
    }

    private List<Symbol> mapStock (List<Stock> listStocks){
        return listStocks.stream().map(stock -> {
            return Symbol.builder()
                    .id(stock.getStock())
                    .last(stock.getLast())
                    .build();
        }).collect(Collectors.toList());
    }
}
