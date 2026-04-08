package com.personal.marketnote.reward.adapter.in.web.gifticon.response;

import com.personal.marketnote.reward.port.in.result.gifticon.GetGiftishowBizMoneyBalanceResult;

public record GetGiftishowBizMoneyBalanceResponse(long balance) {

    public static GetGiftishowBizMoneyBalanceResponse from(GetGiftishowBizMoneyBalanceResult result) {
        return new GetGiftishowBizMoneyBalanceResponse(result.balance());
    }
}
