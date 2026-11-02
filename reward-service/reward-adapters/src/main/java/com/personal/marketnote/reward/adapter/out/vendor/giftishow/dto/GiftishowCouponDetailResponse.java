package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GiftishowCouponDetailResponse(
        @JsonProperty("tr_id") String trId,
        @JsonProperty("order_no") String orderNo,
        @JsonProperty("pin_status_cd") String pinStatusCd,
        @JsonProperty("pin_status_nm") String pinStatusNm,
        @JsonProperty("valid_prd_end_dt") String validPrdEndDt,
        @JsonProperty("goods_code") String goodsCode,
        @JsonProperty("goods_name") String goodsName
) {
}
