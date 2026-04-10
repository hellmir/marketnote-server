package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GiftishowBrandDetailResponse(
        @JsonProperty("brand_code") String brandCode,
        @JsonProperty("brand_name") String brandName,
        @JsonProperty("brand_icon_img") String brandIconImg,
        @JsonProperty("category1Seq") String category1Seq,
        @JsonProperty("category1Name") String category1Name
) {
}
