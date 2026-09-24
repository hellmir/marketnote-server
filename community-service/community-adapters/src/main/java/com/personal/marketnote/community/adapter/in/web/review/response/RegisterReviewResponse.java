package com.personal.marketnote.community.adapter.in.web.review.response;

import com.personal.marketnote.community.port.in.result.review.RegisterReviewResult;

import java.util.UUID;

public record RegisterReviewResponse(
        Long id,
        UUID reviewKey
) {
    public static RegisterReviewResponse from(RegisterReviewResult result) {
        return new RegisterReviewResponse(result.id(), result.reviewKey());
    }
}
