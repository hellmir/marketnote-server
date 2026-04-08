package com.personal.marketnote.reward.port.in.command.gifticon;

public record PurchaseGifticonCommand(
        Long userId,
        String goodsCode
) {
}
