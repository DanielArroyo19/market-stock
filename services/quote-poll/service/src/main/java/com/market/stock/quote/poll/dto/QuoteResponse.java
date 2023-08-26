package com.market.stock.quote.poll.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@JsonRootName("quoteResponse")
@Getter
@Setter
@NoArgsConstructor
public class QuoteResponse implements Serializable {
    @JsonProperty("result")
    private List<Result> result;
    @JsonProperty("error")
    private Error error;
}
