package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.UpdateUserTermResult;

public record UpdateUserTermResponse(
        Long id,
        String content,
        Boolean isRequired,
        Boolean isAgreed
) {
    public static UpdateUserTermResponse from(UpdateUserTermResult updateUserTermResult) {
        return new UpdateUserTermResponse(
                updateUserTermResult.id(),
                updateUserTermResult.content(),
                updateUserTermResult.isRequired(),
                updateUserTermResult.isAgreed()
        );
    }
}
