package com.personal.marketnote.reward.port.in.result.gifticon;

import java.util.List;

public record GetPopularGifticonGoodsResult(List<PopularGifticonGoodsItem> items) {

    public record PopularGifticonGoodsItem(
            String goodsCode,
            String goodsName,
            String brandCode,
            String brandName,
            String brandImageUrl,
            Long salePrice,
            Long cashPrice,
            String imageUrl
    ) {
    }
}
