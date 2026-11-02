package com.personal.marketnote.community.port.in.result.post;

import java.util.List;

public record GetUserOneOnOneInquiryPostsResult(
        int page,
        int pageSize,
        long totalElements,
        int totalPages,
        List<PostItemResult> posts
) {
    public static GetUserOneOnOneInquiryPostsResult of(
            int page,
            int pageSize,
            long totalElements,
            int totalPages,
            List<PostItemResult> posts
    ) {
        return new GetUserOneOnOneInquiryPostsResult(page, pageSize, totalElements, totalPages, posts);
    }
}
