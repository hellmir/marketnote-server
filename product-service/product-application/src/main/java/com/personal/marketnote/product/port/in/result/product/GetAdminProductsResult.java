package com.personal.marketnote.product.port.in.result.product;

import java.util.List;

public record GetAdminProductsResult(
        int page,
        int pageSize,
        long totalElements,
        int totalPages,
        List<ProductItemResult> products
) {
    public static GetAdminProductsResult of(
            int page,
            int pageSize,
            long totalElements,
            int totalPages,
            List<ProductItemResult> products
    ) {
        return new GetAdminProductsResult(page, pageSize, totalElements, totalPages, products);
    }
}
