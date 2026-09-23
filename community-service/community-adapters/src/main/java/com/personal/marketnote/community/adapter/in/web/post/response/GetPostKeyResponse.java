package com.personal.marketnote.community.adapter.in.web.post.response;

import com.personal.marketnote.community.port.in.result.post.GetPostKeyResult;

import java.util.UUID;

public record GetPostKeyResponse(
        UUID postKey
) {
    public static GetPostKeyResponse from(GetPostKeyResult result) {
        return new GetPostKeyResponse(result.postKey());
    }
}
