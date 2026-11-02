package com.personal.marketnote.community.adapter.in.web.review.response;

import com.personal.marketnote.community.port.in.result.review.UpdateReviewResult;

import java.util.UUID;

public record UpdateReviewResponse(
        Long id,
        UUID reviewKey
) {
    public static UpdateReviewResponse from(UpdateReviewResult result) {
        return new UpdateReviewResponse(result.id(), result.reviewKey());
    }
}
