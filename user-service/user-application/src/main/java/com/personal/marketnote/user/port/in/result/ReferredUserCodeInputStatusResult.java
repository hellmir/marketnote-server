package com.personal.marketnote.user.port.in.result;

import com.personal.marketnote.user.domain.user.User;

public record ReferredUserCodeInputStatusResult(
    boolean isRegistered,
    boolean isSkipped
) {
    public static ReferredUserCodeInputStatusResult from(User user) {
        return new ReferredUserCodeInputStatusResult(
            user.hasReferredUserCode(),
            user.isReferredUserCodeInputSkipped()
        );
    }
}
