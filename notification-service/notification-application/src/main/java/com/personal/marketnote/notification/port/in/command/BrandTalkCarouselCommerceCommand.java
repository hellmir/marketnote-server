package com.personal.marketnote.notification.port.in.command;

public record BrandTalkCarouselCommerceCommand(
        String title,
        String regularPrice,
        String discountPrice,
        String discountRate,
        String discountFixed
) {
}
