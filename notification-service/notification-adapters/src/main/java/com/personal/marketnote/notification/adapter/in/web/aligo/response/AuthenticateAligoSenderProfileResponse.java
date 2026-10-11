package com.personal.marketnote.notification.adapter.in.web.aligo.response;

import com.personal.marketnote.notification.port.in.result.aligo.AuthenticateAligoSenderProfileResult;

public record AuthenticateAligoSenderProfileResponse(
        String senderKey,
        String plusId
) {

    public static AuthenticateAligoSenderProfileResponse from(AuthenticateAligoSenderProfileResult result) {
        return new AuthenticateAligoSenderProfileResponse(result.senderKey(), result.plusId());
    }
}
