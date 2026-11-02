package com.personal.marketnote.community.adapter.in.web.review.response;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.port.in.result.review.GetProductReviewAggregatesResult;

import java.util.List;

public record GetProductReviewAggregatesResponse(
        List<ProductReviewAggregateSummaryResponse> reviewAggregates
) {
    public static GetProductReviewAggregatesResponse from(
            GetProductReviewAggregatesResult result
    ) {
        if (FormatValidator.hasNoValue(result)
                || FormatValidator.hasNoValue(result.reviewAggregates())) {
            return new GetProductReviewAggregatesResponse(List.of());
        }

        return new GetProductReviewAggregatesResponse(
                result.reviewAggregates().stream()
                        .map(ProductReviewAggregateSummaryResponse::from)
                        .toList()
        );
    }
}
