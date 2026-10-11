package com.personal.marketnote.notification.port.in.result.aligo;

public record RegisterAligoSenderProfileResult(
        Long id,
        String senderKey,
        String categoryCode
) {
}
