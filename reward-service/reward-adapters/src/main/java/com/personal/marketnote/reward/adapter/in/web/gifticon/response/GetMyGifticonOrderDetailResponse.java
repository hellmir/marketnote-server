package com.personal.marketnote.reward.adapter.in.web.gifticon.response;

import com.personal.marketnote.reward.port.in.result.gifticon.GetMyGifticonOrderDetailResult;

import java.time.LocalDateTime;

public record GetMyGifticonOrderDetailResponse(
        Long orderId,
        String goodsName,
        String brandName,
        String brandImageUrl,
        String productImageUrl,
        String description,
        Long cashPrice,
        String couponImageUrl,
        String pinNo,
        String expiryDate,
        Integer daysRemaining,
        String statusLabel,
        String orderStatus,
        LocalDateTime createdAt
) {
    public static GetMyGifticonOrderDetailResponse from(GetMyGifticonOrderDetailResult result) {
        return new GetMyGifticonOrderDetailResponse(
                result.orderId(),
                result.goodsName(),
                result.brandName(),
                result.brandImageUrl(),
                result.productImageUrl(),
                result.description(),
                result.cashPrice(),
                result.couponImageUrl(),
                result.pinNo(),
                result.expiryDate(),
                result.daysRemaining(),
                result.statusLabel(),
                result.orderStatus(),
                result.createdAt()
        );
    }
}
