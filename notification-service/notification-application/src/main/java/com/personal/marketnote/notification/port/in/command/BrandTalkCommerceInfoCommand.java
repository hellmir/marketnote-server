package com.personal.marketnote.notification.port.in.command;

public record BrandTalkCommerceInfoCommand(
        String title,
        String regularPrice,
        String discountPrice,
        String discountRate,
        String discountFixed
) {
}
