package com.personal.marketnote.community.adapter.in.web.review.response;

import com.personal.marketnote.community.port.in.result.review.GetReviewKeyResult;

import java.util.UUID;

public record GetReviewKeyResponse(
        UUID reviewKey
) {
    public static GetReviewKeyResponse from(GetReviewKeyResult result) {
        return new GetReviewKeyResponse(result.reviewKey());
    }
}
