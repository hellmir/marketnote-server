package com.personal.marketnote.reward.port.in.result.gifticon;

import java.time.LocalDateTime;
import java.util.List;

public record GetMyGifticonOrdersResult(
        long availableCount,
        long completedOrExpiredCount,
        boolean hasNext,
        Long nextCursor,
        List<MyGifticonOrderItem> items
) {

    public record MyGifticonOrderItem(
            Long orderId,
            String goodsName,
            String brandName,
            String productImageUrl,
            Long cashPrice,
            String expiryDate,
            Integer daysRemaining,
            String statusLabel,
            String orderStatus,
            LocalDateTime createdAt
    ) {
    }
}
