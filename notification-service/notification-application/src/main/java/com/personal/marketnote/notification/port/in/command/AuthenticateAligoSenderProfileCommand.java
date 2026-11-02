package com.personal.marketnote.notification.port.in.command;

public record AuthenticateAligoSenderProfileCommand(
        String plusId,
        String phoneNumber
) {
}
