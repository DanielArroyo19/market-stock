package com.market.stock;

import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;

@Builder(toBuilder = true)
@Value
public class Stock {
    String stock;
    ZonedDateTime enabled;
    Double last;
}
