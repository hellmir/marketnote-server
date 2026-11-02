package com.personal.marketnote.community.port.in.result.review;

import com.personal.marketnote.common.utility.FormatValidator;

import java.util.List;

public record GetProductReviewAggregatesResult(
        List<ProductReviewAggregateSummaryResult> reviewAggregates
) {
    public static GetProductReviewAggregatesResult from(
            List<ProductReviewAggregateSummaryResult> reviewAggregates
    ) {
        if (FormatValidator.hasNoValue(reviewAggregates)) {
            return new GetProductReviewAggregatesResult(List.of());
        }

        return new GetProductReviewAggregatesResult(reviewAggregates);
    }

    public static GetProductReviewAggregatesResult empty() {
        return new GetProductReviewAggregatesResult(List.of());
    }
}
