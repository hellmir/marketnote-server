package com.personal.marketnote.reward.adapter.in.web.gifticon.response;

import com.personal.marketnote.reward.port.in.result.gifticon.GetPopularGifticonGoodsResult;

import java.util.List;

public record GetPopularGifticonGoodsResponse(List<PopularGifticonGoodsItemResponse> items) {

    public static GetPopularGifticonGoodsResponse from(GetPopularGifticonGoodsResult result) {
        List<PopularGifticonGoodsItemResponse> items = result.items().stream()
                .map(item -> new PopularGifticonGoodsItemResponse(
                        item.goodsCode(),
                        item.goodsName(),
                        item.brandCode(),
                        item.brandName(),
                        item.brandImageUrl(),
                        item.salePrice(),
                        item.cashPrice(),
                        item.imageUrl()
                ))
                .toList();

        return new GetPopularGifticonGoodsResponse(items);
    }

    public record PopularGifticonGoodsItemResponse(
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
