package com.personal.marketnote.reward.port.in.command.gifticon;

import java.util.List;

public record ManageGifticonCategoryOrderCommand(
        List<OrderItem> items
) {

    public record OrderItem(
            Long categoryId,
            Integer orderNum
    ) {
    }
}
