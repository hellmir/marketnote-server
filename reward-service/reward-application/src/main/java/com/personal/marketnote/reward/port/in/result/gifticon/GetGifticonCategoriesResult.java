package com.personal.marketnote.reward.port.in.result.gifticon;

import java.util.List;

public record GetGifticonCategoriesResult(List<GifticonCategoryItem> categories) {

    public record GifticonCategoryItem(
            String categoryCode,
            String displayName,
            String iconUrl,
            Integer orderNum
    ) {
    }
}
