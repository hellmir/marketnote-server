package com.personal.marketnote.reward.port.in.command.gifticon;

import java.util.List;

public record ManageFeaturedGifticonGoodsCommand(
        List<FeaturedGoodsItem> items
) {
    public record FeaturedGoodsItem(
            String goodsCode,
            boolean popular,
            Integer popularOrderNum
    ) {
    }
}
