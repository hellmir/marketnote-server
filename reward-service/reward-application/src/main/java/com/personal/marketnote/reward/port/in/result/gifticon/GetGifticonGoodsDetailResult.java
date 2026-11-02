package com.personal.marketnote.reward.port.in.result.gifticon;

public record GetGifticonGoodsDetailResult(
        String goodsCode,
        String goodsName,
        String brandCode,
        String brandName,
        String brandImageUrl,
        String categoryCode,
        Long realPrice,
        Long salePrice,
        Long cashPrice,
        String imageUrl,
        String description,
        Integer validDays,
        Long userCashBalance
) {
}
