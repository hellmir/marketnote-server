package com.personal.marketnote.community.adapter.in.web.post.response;

import com.personal.marketnote.common.adapter.in.response.OffsetResponse;
import com.personal.marketnote.community.port.in.result.post.GetUserProductInquiryPostsResult;

public record GetUserProductInquiryPostsResponse(OffsetResponse<PostItemResponse> posts) {
    public static GetUserProductInquiryPostsResponse from(GetUserProductInquiryPostsResult result) {
        return new GetUserProductInquiryPostsResponse(
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
