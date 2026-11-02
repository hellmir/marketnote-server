package com.personal.marketnote.product.port.out.result;

import java.math.BigDecimal;

public record ProductReviewAggregateResult(
        Long productId,
        Integer totalCount,
        BigDecimal averageRating
) {
    public static ProductReviewAggregateResult empty(Long productId) {
        return new ProductReviewAggregateResult(productId, 0, BigDecimal.ZERO);
    }
}
