package com.personal.marketnote.notification.adapter.in.web.aligo.response;

import com.personal.marketnote.notification.port.in.result.aligo.RegisterAligoSenderProfileResult;

public record RegisterAligoSenderProfileResponse(
        Long id,
        String senderKey,
        String categoryCode
) {

    public static RegisterAligoSenderProfileResponse from(RegisterAligoSenderProfileResult result) {
        return new RegisterAligoSenderProfileResponse(result.id(), result.senderKey(), result.categoryCode());
    }
}
