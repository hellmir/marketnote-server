package com.personal.marketnote.reward.adapter.in.web.gifticon.response;

import com.personal.marketnote.common.adapter.in.response.CursorResponse;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonGoodsResult;

public record GetGifticonGoodsResponse(CursorResponse<GifticonGoodsItemResponse> goods) {

    public static GetGifticonGoodsResponse from(GetGifticonGoodsResult result) {
        return new GetGifticonGoodsResponse(
                new CursorResponse<>(
                        result.totalElements(),
                        result.hasNext(),
                        result.nextCursor(),
                        result.items().stream()
                                .map(item -> new GifticonGoodsItemResponse(
                                        item.goodsCode(),
                                        item.goodsName(),
                                        item.brandCode(),
                                        item.brandName(),
                                        item.brandImageUrl(),
                                        item.salePrice(),
                                        item.cashPrice(),
                                        item.imageUrl(),
                                        item.orderNum()
                                ))
                                .toList()
                )
        );
    }

    public record GifticonGoodsItemResponse(
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
