package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GiftishowCouponDetailResponse(
        List<CouponInfo> couponInfoList
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CouponInfo(
            String trId,
            String orderNo,
            String pinStatusCd,
            String pinStatusNm,
            String validPrdEndDt,
            String goodsCd,
            String goodsNm
    ) {
    }
}
