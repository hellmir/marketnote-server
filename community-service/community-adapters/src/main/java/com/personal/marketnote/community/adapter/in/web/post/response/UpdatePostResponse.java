package com.personal.marketnote.community.adapter.in.web.post.response;

import com.personal.marketnote.community.port.in.result.post.UpdatePostResult;

import java.util.UUID;

public record UpdatePostResponse(
        Long id,
        UUID postKey
) {
    public static UpdatePostResponse from(UpdatePostResult result) {
        return new UpdatePostResponse(result.id(), result.postKey());
    }
}
