package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GiftishowProductListResponse(
        @JsonProperty("list_total_cnt") int listTotalCnt,
        @JsonProperty("goodsList") List<GiftishowProductItem> goodsList
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GiftishowProductItem(
            @JsonProperty("goods_code") String goodsCode,
            @JsonProperty("goods_name") String goodsName,
            @JsonProperty("goods_img_B") String goodsImgB,
            @JsonProperty("brandCode") String brandCode,
            @JsonProperty("brandName") String brandName,
            @JsonProperty("brandIconImg") String brandIconImg,
            @JsonProperty("category1Seq") String category1Seq,
            @JsonProperty("sale_price") long salePrice,
            @JsonProperty("real_price") long realPrice,
            @JsonProperty("limitDay") int limitDay,
            @JsonProperty("content") String content,
            @JsonProperty("goods_status") String goodsStatus
    ) {
    }
}
