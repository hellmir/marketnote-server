package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GiftishowBrandListResponse(
    int listNum,
    List<GiftishowBrandItem> brandList
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GiftishowBrandItem(
        String brandCode,
        String brandName,
        String brandIconImg,
        String category1Seq,
        String category1Name
    ) {
    }
}
