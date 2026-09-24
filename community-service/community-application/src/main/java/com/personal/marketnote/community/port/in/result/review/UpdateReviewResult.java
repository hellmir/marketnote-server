package com.personal.marketnote.community.port.in.result.review;

import com.personal.marketnote.community.domain.review.Review;

import java.util.UUID;

public record UpdateReviewResult(
        Long id,
        UUID reviewKey
) {
    public static UpdateReviewResult from(Review review) {
        return new UpdateReviewResult(review.getId(), review.getReviewKey());
    }
}
