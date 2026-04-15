package com.personal.marketnote.community.port.in.result.review;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.community.domain.review.Review;

import java.util.List;
import java.util.Map;

public record GetMyReviewsResult(
        Long totalElements,
        Long nextCursor,
        boolean hasNext,
        List<MyReviewItemResult> reviews
) {
    public static GetMyReviewsResult from(
            boolean hasNext,
            Long nextCursor,
            Long totalElements,
            List<Review> reviews,
            Map<Long, List<GetFileResult>> reviewImagesByReviewId,
            Map<Long, ReviewProductInfoResult> productInfoByPricePolicyId
    ) {
        return new GetMyReviewsResult(
                totalElements,
                nextCursor,
                hasNext,
                reviews.stream()
                        .map(review -> {
                            ReviewProductInfoResult productInfo =
                                    ReviewProductInfoResult.resolveForReview(
                                            review, productInfoByPricePolicyId
                                    );

                            return MyReviewItemResult.from(
                                    review,
                                    reviewImagesByReviewId.get(review.getId()),
                                    productInfo
                            );
                        })
                        .toList()
        );
    }
}
