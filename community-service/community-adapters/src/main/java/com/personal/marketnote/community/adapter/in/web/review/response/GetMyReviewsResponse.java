package com.personal.marketnote.community.adapter.in.web.review.response;

import com.personal.marketnote.common.adapter.in.response.CursorResponse;
import com.personal.marketnote.community.port.in.result.review.GetMyReviewsResult;

import java.util.stream.Collectors;

public record GetMyReviewsResponse(
        CursorResponse<MyReviewItemResponse> reviews
) {
    public static GetMyReviewsResponse from(GetMyReviewsResult result) {
        return new GetMyReviewsResponse(
                new CursorResponse<>(
                        result.totalElements(),
                        result.hasNext(),
                        result.nextCursor(),
                        result.reviews().stream()
                                .map(MyReviewItemResponse::from)
                                .collect(Collectors.toList())
                )
        );
    }
}
