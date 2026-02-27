package com.personal.marketnote.commerce.port.out.product.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductPricePolicyInfoResult(
        Long id,
        Long price,
        Long discountPrice
) {
}
