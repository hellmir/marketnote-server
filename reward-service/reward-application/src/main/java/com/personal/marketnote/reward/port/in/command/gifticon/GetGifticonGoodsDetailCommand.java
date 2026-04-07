package com.personal.marketnote.reward.port.in.command.gifticon;

public record GetGifticonGoodsDetailCommand(
        String goodsCode,
        Long userId
) {
}
