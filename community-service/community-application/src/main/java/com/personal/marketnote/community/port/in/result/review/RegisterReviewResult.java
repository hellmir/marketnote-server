package com.personal.marketnote.community.port.in.result.review;

import com.personal.marketnote.community.domain.review.Review;

import java.util.UUID;

public record RegisterReviewResult(
        Long id,
        UUID reviewKey
) {
    public static RegisterReviewResult from(Review review) {
        return new RegisterReviewResult(review.getId(), review.getReviewKey());
    }
}
