package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.CheckNicknameResult;

public record CheckNicknameResponse(
        boolean isDuplicated,
        boolean containsProfanity
) {
    public static CheckNicknameResponse from(CheckNicknameResult result) {
        return new CheckNicknameResponse(result.isDuplicated(), result.containsProfanity());
    }
}
