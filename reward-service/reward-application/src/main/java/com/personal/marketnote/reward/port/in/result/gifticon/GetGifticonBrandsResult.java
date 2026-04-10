package com.personal.marketnote.reward.port.in.result.gifticon;

import java.util.List;

public record GetGifticonBrandsResult(List<GifticonBrandItem> brands) {

    public record GifticonBrandItem(
            String brandCode,
            String brandName,
            String brandImageUrl
    ) {
    }
}
