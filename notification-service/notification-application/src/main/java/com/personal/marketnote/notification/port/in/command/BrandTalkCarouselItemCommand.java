package com.personal.marketnote.notification.port.in.command;

import java.util.List;

public record BrandTalkCarouselItemCommand(
        String header,
        String content,
        List<BrandTalkButtonCommand> buttons,
        BrandTalkCouponCommand coupon
) {
}
