package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GiftishowBizMoneyResponse(
        String code,
        String message,
        long balance
) {

    public boolean isSuccess() {
        return "0000".equals(code);
    }
}
