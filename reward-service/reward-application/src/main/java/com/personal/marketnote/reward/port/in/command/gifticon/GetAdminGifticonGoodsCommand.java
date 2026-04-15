package com.personal.marketnote.reward.port.in.command.gifticon;

public record GetAdminGifticonGoodsCommand(
        int page,
        int pageSize,
        String goodsStatus,
        Boolean exposed,
        String keyword
) {
}
