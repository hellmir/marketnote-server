package com.personal.marketnote.community.adapter.in.web.review.response;

import com.personal.marketnote.community.port.in.result.review.ProductReviewAggregateResult;

import java.math.BigDecimal;

public record GetProductReviewAggregateResponse(
        int totalCount,
        int fivePointCount,
        int fourPointCount,
        int threePointCount,
        int twoPointCount,
        int onePointCount,
        BigDecimal averageRating
) {
    public static GetProductReviewAggregateResponse from(ProductReviewAggregateResult result) {
        return new GetProductReviewAggregateResponse(
                result.getTotalCount(),
                result.getFivePointCount(),
                result.getFourPointCount(),
                result.getThreePointCount(),
                result.getTwoPointCount(),
                result.getOnePointCount(),
                result.getAverageRating()
        );
    }
}
