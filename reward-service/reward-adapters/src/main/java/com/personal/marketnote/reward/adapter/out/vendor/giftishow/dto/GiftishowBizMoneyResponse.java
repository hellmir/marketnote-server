package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GiftishowBizMoneyResponse(
        @JsonProperty("user_id") String userId,
        @JsonProperty("biz_money") long bizMoney
) {
}
