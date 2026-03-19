package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.CheckNicknameResult;

public record CheckNicknameResponse(
        boolean isDuplicated
) {
    public static CheckNicknameResponse from(CheckNicknameResult result) {
        return new CheckNicknameResponse(result.isDuplicated());
    }
}
