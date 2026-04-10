package com.personal.marketnote.reward.port.in.result.gifticon;

import java.util.List;

public record GetGifticonGoodsResult(
        int page,
        int pageSize,
        long totalElements,
        int totalPages,
        List<GifticonGoodsItem> items
) {

    public record GifticonGoodsItem(
            String goodsCode,
            String goodsName,
            String brandCode,
            String brandName,
            String brandImageUrl,
            Long salePrice,
            Long cashPrice,
            String imageUrl,
            Integer orderNum
    ) {
    }
}
