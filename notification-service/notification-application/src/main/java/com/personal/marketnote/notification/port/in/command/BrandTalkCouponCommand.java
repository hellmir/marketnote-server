package com.personal.marketnote.notification.port.in.command;

public record BrandTalkCouponCommand(
        String title,
        String description,
        String linkM,
        String linkP,
        String linkI,
        String linkA
) {
}
