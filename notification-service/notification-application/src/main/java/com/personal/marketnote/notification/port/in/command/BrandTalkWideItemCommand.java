package com.personal.marketnote.notification.port.in.command;

public record BrandTalkWideItemCommand(
        String title,
        String imageUrl,
        String linkM,
        String linkP,
        String linkI,
        String linkA
) {
}
