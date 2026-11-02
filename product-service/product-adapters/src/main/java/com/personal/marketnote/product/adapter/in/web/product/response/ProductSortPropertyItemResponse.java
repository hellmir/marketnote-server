package com.personal.marketnote.product.adapter.in.web.product.response;

import com.personal.marketnote.product.port.in.result.product.ProductSortPropertyItemResult;

public record ProductSortPropertyItemResponse(
        String name,
        String description
) {

    public static ProductSortPropertyItemResponse from(ProductSortPropertyItemResult result) {
        return new ProductSortPropertyItemResponse(
                result.name(),
                result.description()
        );
    }
}


