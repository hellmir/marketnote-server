package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GiftishowProductListResponse(
    int listNum,
    List<GiftishowProductItem> goodsList
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GiftishowProductItem(
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
