package com.personal.marketnote.community.adapter.in.web.review.response;

import com.personal.marketnote.community.port.in.result.review.GetReviewCountResult;

public record GetReviewsCountResponse(long totalCount) {
    public static GetReviewsCountResponse from(GetReviewCountResult result) {
        return new GetReviewsCountResponse(result.totalCount());
    }
}
