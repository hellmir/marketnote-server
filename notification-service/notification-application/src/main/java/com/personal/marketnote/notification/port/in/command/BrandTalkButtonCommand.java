package com.personal.marketnote.notification.port.in.command;

public record BrandTalkButtonCommand(
        String name,
        String linkType,
        String linkM,
        String linkP,
        String linkI,
        String linkA
) {
}
