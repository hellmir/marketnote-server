package com.personal.marketnote.community.port.in.result.post;

import java.util.List;

public record GetUserProductInquiryPostsResult(
        int page,
        int pageSize,
        long totalElements,
        int totalPages,
        List<UserProductInquiryPostItemResult> posts
) {
    public static GetUserProductInquiryPostsResult of(
            int page,
            int pageSize,
            long totalElements,
            int totalPages,
            List<UserProductInquiryPostItemResult> posts
    ) {
        return new GetUserProductInquiryPostsResult(page, pageSize, totalElements, totalPages, posts);
    }
}
