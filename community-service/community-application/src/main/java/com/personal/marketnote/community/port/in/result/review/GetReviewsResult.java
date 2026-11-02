package com.personal.marketnote.community.port.in.result.review;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.review.Review;

import java.util.List;
import java.util.Map;

public record GetReviewsResult(
        Long totalElements,
        Long nextCursor,
        boolean hasNext,
        List<ReviewItemResult> reviews
) {
    public static GetReviewsResult from(
            boolean hasNext,
            Long nextCursor,
            Long totalElements,
            List<Review> reviews,
            Map<Long, List<GetFileResult>> reviewImagesByReviewId
    ) {
        return from(hasNext, nextCursor, totalElements, reviews, reviewImagesByReviewId, Map.of());
    }

    public static GetReviewsResult from(
            boolean hasNext,
            Long nextCursor,
            Long totalElements,
            List<Review> reviews,
            Map<Long, List<GetFileResult>> reviewImagesByReviewId,
            Map<Long, ReviewProductInfoResult> productInfoByPricePolicyId
    ) {
        return new GetReviewsResult(
                totalElements,
                nextCursor,
                hasNext,
                reviews.stream()
                        .map(review -> {
                            Long pricePolicyId = review.getPricePolicyId();
                            ReviewProductInfoResult productInfo = FormatValidator.hasValue(pricePolicyId)
                                    ? productInfoByPricePolicyId.get(pricePolicyId)
                                    : null;

                            return ReviewItemResult.from(
                                    review,
                                    reviewImagesByReviewId.get(review.getId()),
                                    productInfo
                            );
                        })
                        .toList()
        );
    }
}
