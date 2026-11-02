package com.personal.marketnote.product.port.in.result.product;

import com.personal.marketnote.product.domain.product.ProductSortProperty;

public record ProductSortPropertyItemResult(
        String name,
        String description
) {
    public static ProductSortPropertyItemResult from(ProductSortProperty property) {
        return new ProductSortPropertyItemResult(
                property.name(),
                property.getDescription()
        );
    }
}
