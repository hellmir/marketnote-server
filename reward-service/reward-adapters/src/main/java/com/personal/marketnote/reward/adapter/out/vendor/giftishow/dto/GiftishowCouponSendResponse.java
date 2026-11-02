package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GiftishowCouponSendResponse(
        @JsonProperty("tr_id") String trId,
        @JsonProperty("order_no") String orderNo,
        @JsonProperty("pin_no") String pinNo,
        @JsonProperty("coupon_img_url") String couponImgUrl,
        @JsonProperty("valid_prd_end_dt") String validPrdEndDt
) {
}
