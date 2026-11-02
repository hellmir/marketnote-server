package com.personal.marketnote.product.port.in.result.product;

import com.personal.marketnote.product.domain.product.ProductSearchTarget;

import java.util.Arrays;

public record GetProductSearchTargetsResult(
        ProductSearchTargetItemResult[] targets
) {
    public static GetProductSearchTargetsResult from(ProductSearchTarget[] targets) {
        return new GetProductSearchTargetsResult(Arrays.stream(targets)
                .map(ProductSearchTargetItemResult::from)
                .toArray(ProductSearchTargetItemResult[]::new));
    }
}
