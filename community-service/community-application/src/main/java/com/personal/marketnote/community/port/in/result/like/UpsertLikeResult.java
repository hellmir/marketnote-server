package com.personal.marketnote.community.port.in.result.like;

import com.personal.marketnote.community.domain.like.Like;

public record UpsertLikeResult(
        boolean isLiked,
        boolean isNew
) {
    public static UpsertLikeResult from(Like like, boolean isNew) {
        return new UpsertLikeResult(like.isLiked(), isNew);
    }
}
