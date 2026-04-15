package com.personal.marketnote.reward.port.in.result.gifticon;

public record GetGiftishowBizMoneyBalanceResult(
        long balance
) {
    public static GetGiftishowBizMoneyBalanceResult of(long balance) {
        return new GetGiftishowBizMoneyBalanceResult(balance);
    }
}
