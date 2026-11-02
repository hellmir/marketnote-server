package com.personal.marketnote.community.adapter.in.web.post.response;

import com.personal.marketnote.community.port.in.result.post.RegisterPostResult;

import java.util.UUID;

public record RegisterPostResponse(
        Long id,
        UUID postKey
) {
    public static RegisterPostResponse from(RegisterPostResult result) {
        return new RegisterPostResponse(result.id(), result.postKey());
    }
}
