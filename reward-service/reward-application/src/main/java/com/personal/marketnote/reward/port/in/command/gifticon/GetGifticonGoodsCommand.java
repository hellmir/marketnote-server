package com.personal.marketnote.reward.port.in.command.gifticon;

public record GetGifticonGoodsCommand(
        String categoryCode,
        String brandCode,
        int page,
        int pageSize
) {
}
