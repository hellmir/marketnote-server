package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GiftishowProductDetailResponse(
    GoodsDetail goodsDetail
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GoodsDetail(
        String goodsCode,
        String goodsName,
        String goodsImgB,
        String brandCode,
        String brandName,
        String brandIconImg,
        String category1Seq,
        long salePrice,
        long realPrice,
        int limitDay,
        String content,
        String goodsStateCd
    ) {
    }
}
