package com.personal.marketnote.commerce.port.out.product.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductOptionInfoResult(
        Long id,
        String content,
        String status
) {
}
