package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GiftishowCouponDetailResponse(
        String trId,
        String orderNo,
        String pinStatusCd,
        String pinStatusNm,
        String validPrdEndDt,
        String goodsCode,
        String goodsName
) {
}
