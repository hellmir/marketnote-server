package com.personal.marketnote.reward.port.in.command.gifticon;

import java.util.List;

public record ManageGifticonGoodsExposureCommand(
        List<ExposureItem> items
) {
    public record ExposureItem(
            String goodsCode,
            boolean exposed
    ) {
    }
}
