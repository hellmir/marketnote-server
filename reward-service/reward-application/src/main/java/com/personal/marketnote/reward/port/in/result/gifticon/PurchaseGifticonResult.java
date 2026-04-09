package com.personal.marketnote.reward.port.in.result.gifticon;

public record PurchaseGifticonResult(
        Long orderId,
        String orderNo,
        Long cashAmount,
        String goodsName
) {
}
