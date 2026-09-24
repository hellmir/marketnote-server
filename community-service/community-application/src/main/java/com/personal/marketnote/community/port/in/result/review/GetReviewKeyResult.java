package com.personal.marketnote.community.port.in.result.review;

import com.personal.marketnote.community.domain.review.Review;

import java.util.UUID;

public record GetReviewKeyResult(
        UUID reviewKey
) {
    public static GetReviewKeyResult from(Review review) {
        return new GetReviewKeyResult(review.getReviewKey());
    }
}
