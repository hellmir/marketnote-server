package com.personal.marketnote.community.port.in.result.post;

import com.personal.marketnote.community.domain.post.Post;

import java.util.UUID;

public record UpdatePostResult(
        Long id,
        UUID postKey
) {
    public static UpdatePostResult from(Post post) {
        return new UpdatePostResult(post.getId(), post.getPostKey());
    }
}
