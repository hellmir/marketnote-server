package com.personal.marketnote.product.adapter.in.web.product.response;

import com.personal.marketnote.product.port.in.result.product.ProductSearchTargetItemResult;

public record ProductSearchTargetItemResponse(
        String name,
        String description
) {
    public static ProductSearchTargetItemResponse from(ProductSearchTargetItemResult result) {
        return new ProductSearchTargetItemResponse(
                result.name(),
                result.description()
        );
    }
}


