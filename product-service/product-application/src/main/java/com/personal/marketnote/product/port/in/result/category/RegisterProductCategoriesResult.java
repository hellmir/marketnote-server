package com.personal.marketnote.product.port.in.result.category;

import java.util.List;

public record RegisterProductCategoriesResult(
        Long productId,
        List<Long> categoryIds
) {
    public static RegisterProductCategoriesResult of(Long productId, List<Long> categoryIds) {
        return new RegisterProductCategoriesResult(productId, categoryIds);
    }
}
