package com.personal.marketnote.reward.port.in.result.gifticon;

public record GetGifticonVendorBalanceResult(
        long balance
) {
    public static GetGifticonVendorBalanceResult of(long balance) {
        return new GetGifticonVendorBalanceResult(balance);
    }
}
