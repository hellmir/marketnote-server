package com.personal.marketnote.notification.port.in.command;

import java.util.List;

public record RegisterBrandTalkTemplateCommand(
        String senderKey,
        String templateCode,
        String templateName,
        String templateType,
        String templateContent,
        boolean adult,
        String imageLink,
        List<BrandTalkButtonCommand> buttons,
        BrandTalkCouponCommand coupon,
        byte[] imageFile,
        String imageFileName
) {
}
