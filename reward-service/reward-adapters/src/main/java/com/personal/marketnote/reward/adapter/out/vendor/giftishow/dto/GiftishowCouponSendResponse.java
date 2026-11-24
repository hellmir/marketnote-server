package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GiftishowCouponSendResponse(
        String code,
        String message,
        InnerResult result
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InnerResult(
            String orderNo,
            String pinNo,
            String couponImgUrl
    ) {
    }
}
