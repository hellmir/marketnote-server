package com.personal.marketnote.community.port.in.result.post;

import com.personal.marketnote.community.domain.post.Post;

import java.util.UUID;

public record GetPostKeyResult(
        UUID postKey
) {
    public static GetPostKeyResult from(Post post) {
        return new GetPostKeyResult(post.getPostKey());
    }
}
