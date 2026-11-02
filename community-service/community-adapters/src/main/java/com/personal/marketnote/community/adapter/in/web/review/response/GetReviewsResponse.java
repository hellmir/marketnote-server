package com.personal.marketnote.community.adapter.in.web.review.response;

import com.personal.marketnote.common.adapter.in.response.CursorResponse;
import com.personal.marketnote.community.port.in.result.review.GetReviewsResult;

import java.util.stream.Collectors;

public record GetReviewsResponse(
        CursorResponse<ReviewItemResponse> reviews
) {
    public static GetReviewsResponse from(GetReviewsResult result) {
        return new GetReviewsResponse(
                new CursorResponse<>(
                        result.totalElements(),
                        result.hasNext(),
                        result.nextCursor(),
                        result.reviews().stream()
                                .map(ReviewItemResponse::from)
                                .collect(Collectors.toList())
                )
        );
    }
}
