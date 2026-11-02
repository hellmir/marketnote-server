package com.personal.marketnote.user.adapter.in.web.user.response;

import java.util.UUID;

public record GetUserKeyResponse(
        UUID userKey
) {
    public static GetUserKeyResponse of(UUID userKey) {
        return new GetUserKeyResponse(userKey);
    }
}
