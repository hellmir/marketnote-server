package com.personal.marketnote.community.port.in.result.review;

public record GetReviewCountResult(long totalCount) {
    public static GetReviewCountResult of(long totalCount) {
        return new GetReviewCountResult(totalCount);
    }
}
