package com.personal.marketnote.notification.port.in.command;

import java.util.List;

public record UpdateAligoTemplateCommand(
        String templateCode,
        String senderKey,
        String templateName,
        String templateContent,
        String templateTitle,
        String templateSubtitle,
        List<AligoTemplateButtonCommand> buttons
) {
}
