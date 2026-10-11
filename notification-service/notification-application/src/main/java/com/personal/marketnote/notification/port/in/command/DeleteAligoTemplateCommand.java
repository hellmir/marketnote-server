package com.personal.marketnote.notification.port.in.command;

public record DeleteAligoTemplateCommand(
        String templateCode,
        String senderKey
) {
}
