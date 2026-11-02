package com.personal.marketnote.product.port.in.result.product;

import com.personal.marketnote.product.domain.product.ProductSearchTarget;

public record ProductSearchTargetItemResult(
        String name,
        String description
) {
    public static ProductSearchTargetItemResult from(ProductSearchTarget target) {
        return new ProductSearchTargetItemResult(
                target.name(),
                target.getDescription()
        );
    }
}
