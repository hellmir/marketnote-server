package com.personal.marketnote.notification.port.in.command;

public record RegisterAligoSenderProfileCommand(
        String senderKey,
        String categoryCode
) {
}
