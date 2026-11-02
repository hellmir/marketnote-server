package com.personal.marketnote.community.adapter.in.web.post.response;

import com.personal.marketnote.common.adapter.in.response.CursorResponse;
import com.personal.marketnote.community.port.in.result.post.GetPostsResult;

import java.util.stream.Collectors;

public record GetPostsResponse(
        CursorResponse<PostItemResponse> posts
) {
    public static GetPostsResponse from(GetPostsResult result) {
        return new GetPostsResponse(
                new CursorResponse<>(
                        result.totalElements(),
                        result.hasNext(),
                        result.nextCursor(),
                        result.posts().stream()
                                .map(PostItemResponse::from)
                                .collect(Collectors.toList())
                )
        );
    }
}
