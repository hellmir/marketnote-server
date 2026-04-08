package com.personal.marketnote.reward.adapter.in.web.gifticon.response;

import com.personal.marketnote.reward.port.in.result.gifticon.PurchaseGifticonResult;

public record PurchaseGifticonResponse(
        Long orderId,
        String orderNo,
        Long cashAmount,
        String goodsName
) {
    public static PurchaseGifticonResponse from(PurchaseGifticonResult result) {
        return new PurchaseGifticonResponse(
                result.orderId(),
                result.orderNo(),
                result.cashAmount(),
                result.goodsName()
        );
    }
}
