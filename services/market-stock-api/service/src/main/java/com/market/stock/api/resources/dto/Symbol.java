package com.market.stock.api.resources.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Symbol {
    private String id;
    private String name;
}
