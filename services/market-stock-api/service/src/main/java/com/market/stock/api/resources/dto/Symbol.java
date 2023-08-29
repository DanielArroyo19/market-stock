package com.market.stock.api.resources.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Symbol {
    private String id;
    private String name;
    private Double last;
}
