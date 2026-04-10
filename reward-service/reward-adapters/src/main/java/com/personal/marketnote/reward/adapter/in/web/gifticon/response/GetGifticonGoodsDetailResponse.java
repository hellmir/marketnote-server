package com.personal.marketnote.reward.adapter.in.web.gifticon.response;

import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonGoodsDetailResult;

public record GetGifticonGoodsDetailResponse(
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

    public static GetGifticonGoodsDetailResponse from(GetGifticonGoodsDetailResult result) {
        return new GetGifticonGoodsDetailResponse(
                result.goodsCode(),
                result.goodsName(),
                result.brandCode(),
                result.brandName(),
                result.brandImageUrl(),
                result.categoryCode(),
                result.realPrice(),
                result.salePrice(),
                result.cashPrice(),
                result.imageUrl(),
                result.description(),
                result.validDays(),
                result.userCashBalance()
        );
    }
}
