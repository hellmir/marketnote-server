package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GiftishowBrandDetailResponse(
        String brandCode,
        String brandName,
        String brandIconImg,
        String category1Seq,
        String category1Name
) {
}
