package com.personal.marketnote.community.adapter.in.web.like.response;

import com.personal.marketnote.community.port.in.result.like.UpsertLikeResult;

public record UpsertLikeResponse(
        boolean isLiked
) {
    public static UpsertLikeResponse from(UpsertLikeResult result) {
        return new UpsertLikeResponse(result.isLiked());
    }
}
