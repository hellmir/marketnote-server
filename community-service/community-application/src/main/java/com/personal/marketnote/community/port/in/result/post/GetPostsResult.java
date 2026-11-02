package com.personal.marketnote.community.port.in.result.post;

import java.util.List;

public record GetPostsResult(
        Long totalElements,
        Long nextCursor,
        boolean hasNext,
        List<PostItemResult> posts
) {
    public static GetPostsResult of(
            boolean hasNext,
            Long nextCursor,
            Long totalElements,
            List<PostItemResult> posts
    ) {
        return new GetPostsResult(totalElements, nextCursor, hasNext, posts);
    }
}
