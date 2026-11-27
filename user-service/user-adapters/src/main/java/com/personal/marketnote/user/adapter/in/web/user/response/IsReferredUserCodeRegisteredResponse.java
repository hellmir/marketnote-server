package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.ReferredUserCodeInputStatusResult;

public record IsReferredUserCodeRegisteredResponse(
    boolean isRegistered,
    boolean isSkipped
) {
    public static IsReferredUserCodeRegisteredResponse from(ReferredUserCodeInputStatusResult result) {
        return new IsReferredUserCodeRegisteredResponse(result.isRegistered(), result.isSkipped());
    }
}
