package com.market.stock.quote.poll.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class Statics {
    private double lastPrice;
    private int quantity;
    private double ppp;
    private double gain;
}
