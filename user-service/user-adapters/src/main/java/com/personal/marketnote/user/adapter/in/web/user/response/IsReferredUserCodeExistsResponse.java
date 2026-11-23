package com.personal.marketnote.user.adapter.in.web.user.response;

public record IsReferredUserCodeExistsResponse(
        boolean exists
) {
    public static IsReferredUserCodeExistsResponse of(boolean exists) {
        return new IsReferredUserCodeExistsResponse(exists);
    }
}
