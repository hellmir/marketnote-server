package com.personal.marketnote.user.adapter.in.web.user.response;

public record IsReferredUserCodeRegisteredResponse(
        boolean isRegistered
) {
    public static IsReferredUserCodeRegisteredResponse of(boolean isRegistered) {
        return new IsReferredUserCodeRegisteredResponse(isRegistered);
    }
}
