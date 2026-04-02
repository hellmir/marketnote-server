package com.personal.marketnote.community.port.in.result.review;

import java.util.List;

public record GetUserReviewsResult(
        int page,
        int pageSize,
        long totalElements,
        int totalPages,
        List<UserReviewItemResult> reviews
) {
    public static GetUserReviewsResult of(
            int page,
            int pageSize,
            long totalElements,
            int totalPages,
            List<UserReviewItemResult> reviews
    ) {
        return new GetUserReviewsResult(page, pageSize, totalElements, totalPages, reviews);
    }
}
