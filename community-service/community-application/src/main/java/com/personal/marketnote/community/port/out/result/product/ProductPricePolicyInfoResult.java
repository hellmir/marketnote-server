package com.personal.marketnote.community.port.out.result.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductPricePolicyInfoResult(
        Long id,
        Long price,
        Long discountPrice,
        BigDecimal discountRate,
        Long accumulatedPoint
) {
}
