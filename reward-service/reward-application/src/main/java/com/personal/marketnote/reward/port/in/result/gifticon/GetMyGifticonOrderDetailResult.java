package com.personal.marketnote.reward.port.in.result.gifticon;

import java.time.LocalDateTime;

public record GetMyGifticonOrderDetailResult(
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
}
