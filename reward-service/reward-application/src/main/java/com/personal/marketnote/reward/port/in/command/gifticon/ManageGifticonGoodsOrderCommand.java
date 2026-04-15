package com.personal.marketnote.reward.port.in.command.gifticon;

import java.util.List;

public record ManageGifticonGoodsOrderCommand(
        List<OrderItem> items
) {
    public record OrderItem(
            String goodsCode,
            Integer orderNum
    ) {
    }
}
