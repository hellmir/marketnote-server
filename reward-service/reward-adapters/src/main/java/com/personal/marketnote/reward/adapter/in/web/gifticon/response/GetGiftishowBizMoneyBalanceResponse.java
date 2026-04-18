package com.personal.marketnote.reward.adapter.in.web.gifticon.response;

import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonVendorBalanceResult;

public record GetGiftishowBizMoneyBalanceResponse(long balance) {

    public static GetGiftishowBizMoneyBalanceResponse from(GetGifticonVendorBalanceResult result) {
        return new GetGiftishowBizMoneyBalanceResponse(result.balance());
    }
}
