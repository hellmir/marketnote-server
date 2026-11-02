package com.personal.marketnote.community.adapter.in.web.review.response;

import com.personal.marketnote.common.adapter.in.response.OffsetResponse;
import com.personal.marketnote.community.port.in.result.review.GetUserReviewsResult;

public record GetUserReviewsResponse(OffsetResponse<UserReviewItemResponse> reviews) {
    public static GetUserReviewsResponse from(GetUserReviewsResult result) {
        return new GetUserReviewsResponse(
                new OffsetResponse<>(
                        result.page(),
                        result.pageSize(),
                        result.totalElements(),
                        result.totalPages(),
                        result.reviews().stream()
                                .map(UserReviewItemResponse::from)
                                .toList()
                )
        );
    }
}
