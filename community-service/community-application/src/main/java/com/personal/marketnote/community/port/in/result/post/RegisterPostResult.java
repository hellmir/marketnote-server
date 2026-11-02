package com.personal.marketnote.community.port.in.result.post;

import com.personal.marketnote.community.domain.post.Post;

import java.util.UUID;

public record RegisterPostResult(
        Long id,
        UUID postKey
) {
    public static RegisterPostResult from(Post post) {
        return new RegisterPostResult(post.getId(), post.getPostKey());
    }
}
