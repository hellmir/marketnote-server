package com.personal.marketnote.notification.port.in.command;

import java.util.List;

public record RegisterAligoTemplateCommand(
        String senderKey,
        String templateCode,
        String templateName,
        String templateContent,
        String templateTitle,
        String templateSubtitle,
        String templateType,
        String templateEmType,
        boolean templateSecure,
        boolean templateAdvert,
        String imageUrl,
        String imageName,
        List<AligoTemplateButtonCommand> buttons
) {
}
