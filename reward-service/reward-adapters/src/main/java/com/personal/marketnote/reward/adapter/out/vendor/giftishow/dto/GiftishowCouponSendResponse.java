package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GiftishowCouponSendResponse(
        String trId,
        String orderNo,
        String pinNo,
        String couponImgUrl,
        String validPrdEndDt
) {
}
