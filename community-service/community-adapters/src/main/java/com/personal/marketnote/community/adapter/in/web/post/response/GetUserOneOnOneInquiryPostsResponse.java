package com.personal.marketnote.community.adapter.in.web.post.response;

import com.personal.marketnote.common.adapter.in.response.OffsetResponse;
import com.personal.marketnote.community.port.in.result.post.GetUserOneOnOneInquiryPostsResult;

public record GetUserOneOnOneInquiryPostsResponse(OffsetResponse<PostItemResponse> posts) {
    public static GetUserOneOnOneInquiryPostsResponse from(GetUserOneOnOneInquiryPostsResult result) {
        return new GetUserOneOnOneInquiryPostsResponse(
                new OffsetResponse<>(
                        result.page(),
                        result.pageSize(),
                        result.totalElements(),
                        result.totalPages(),
                        result.posts().stream()
                                .map(PostItemResponse::from)
                                .toList()
                )
        );
    }
}
