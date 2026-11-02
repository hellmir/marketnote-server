package com.personal.marketnote.reward.port.in.result.gifticon;

import java.util.List;

public record GetGifticonGoodsResult(
        Long totalElements,
        boolean hasNext,
        Long nextCursor,
        List<GifticonGoodsItem> items
) {

    public static GetGifticonGoodsResult from(
            Long totalElements,
            boolean hasNext,
            Long nextCursor,
            List<GifticonGoodsItem> items
    ) {
        return new GetGifticonGoodsResult(totalElements, hasNext, nextCursor, items);
    }

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
