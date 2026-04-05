package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GiftishowCouponCancelResponse(
        @JsonProperty("tr_id") String trId,
        @JsonProperty("order_no") String orderNo
) {
}
