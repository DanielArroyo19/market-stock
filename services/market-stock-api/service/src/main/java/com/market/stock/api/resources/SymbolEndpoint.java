package com.market.stock.api.resources;

import com.market.stock.api.resources.dto.Symbol;
import com.market.stock.api.resources.services.SymbolService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/symbol")
@CrossOrigin
public class SymbolEndpoint {

    @Resource
    private SymbolService stockService;

    @GetMapping()
    public List<Symbol> getStocks(){
        return stockService.getSymbol();
    }

    @GetMapping(value = "/{id}")
    public Symbol getStockById(@PathVariable("id") String id){
        return stockService.getSymbolById(id);
    }

    @PostMapping
    public void addStock(@RequestBody Symbol symbol){
        stockService.addSymbol(symbol);
    }

    @PutMapping(value = "/{id}")
    public void putLastPrice(@PathVariable("id") String id, @RequestBody Symbol stock){
        stockService.putLastPrice(id, stock);
    }
}

