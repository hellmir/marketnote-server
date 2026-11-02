package com.personal.marketnote.community.port.in.result.review;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.review.ProductReviewAggregate;

import java.math.BigDecimal;

public record ProductReviewAggregateSummaryResult(
        Long productId,
        int totalCount,
        BigDecimal averageRating
) {
    public static ProductReviewAggregateSummaryResult from(
            Long productId,
            ProductReviewAggregate aggregate
    ) {
        if (FormatValidator.hasNoValue(aggregate)) {
            return new ProductReviewAggregateSummaryResult(productId, 0, new BigDecimal("0.00"));
        }

        return new ProductReviewAggregateSummaryResult(
                productId,
                aggregate.getTotalCount(),
                aggregate.getAverageRating()
        );
    }
}
