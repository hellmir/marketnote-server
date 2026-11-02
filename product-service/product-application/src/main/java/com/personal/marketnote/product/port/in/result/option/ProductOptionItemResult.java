package com.personal.marketnote.product.port.in.result.option;

import com.personal.marketnote.product.domain.option.ProductOption;

public record ProductOptionItemResult(
        Long id,
        String content,
        String status
) {
    public static ProductOptionItemResult from(ProductOption option) {
        return new ProductOptionItemResult(
                option.getId(),
                option.getContent(),
                option.getStatus().name()
        );
    }
}
