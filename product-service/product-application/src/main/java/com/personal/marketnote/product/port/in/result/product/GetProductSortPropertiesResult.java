package com.personal.marketnote.product.port.in.result.product;

import com.personal.marketnote.product.domain.product.ProductSortProperty;

import java.util.Arrays;

public record GetProductSortPropertiesResult(
        ProductSortPropertyItemResult[] properties
) {
    public static GetProductSortPropertiesResult from(ProductSortProperty[] properties) {
        return new GetProductSortPropertiesResult(Arrays.stream(properties)
                .map(ProductSortPropertyItemResult::from)
                .toArray(ProductSortPropertyItemResult[]::new));
    }
}
