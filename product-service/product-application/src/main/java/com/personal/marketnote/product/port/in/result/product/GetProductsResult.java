package com.personal.marketnote.product.port.in.result.product;

import java.util.List;

public record GetProductsResult(
        Long totalElements,
        Long nextCursor,
        boolean hasNext,
        List<ProductItemResult> products
) {
    public static GetProductsResult from(
            boolean hasNext,
            Long nextCursor,
            Long totalElements,
            List<ProductItemResult> products
    ) {
        return new GetProductsResult(
                totalElements,
                nextCursor,
                hasNext,
                products
        );
    }
}
