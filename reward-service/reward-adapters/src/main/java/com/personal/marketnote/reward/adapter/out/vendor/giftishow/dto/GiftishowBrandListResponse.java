package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GiftishowBrandListResponse(
        @JsonProperty("list_total_cnt") int listTotalCnt,
        @JsonProperty("brandList") List<GiftishowBrandItem> brandList
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GiftishowBrandItem(
            @JsonProperty("brand_code") String brandCode,
            @JsonProperty("brand_name") String brandName,
            @JsonProperty("brand_icon_img") String brandIconImg,
            @JsonProperty("category1Seq") String category1Seq,
            @JsonProperty("category1Name") String category1Name
    ) {
    }
}
