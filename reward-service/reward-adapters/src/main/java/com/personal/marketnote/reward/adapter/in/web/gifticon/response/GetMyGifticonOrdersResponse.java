package com.personal.marketnote.reward.adapter.in.web.gifticon.response;

import com.personal.marketnote.reward.port.in.result.gifticon.GetMyGifticonOrdersResult;

import java.time.LocalDateTime;
import java.util.List;

public record GetMyGifticonOrdersResponse(
        long availableCount,
        long completedOrExpiredCount,
        boolean hasNext,
        Long nextCursor,
        List<MyGifticonOrderItemResponse> items
) {

    public record MyGifticonOrderItemResponse(
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

    public static GetMyGifticonOrdersResponse from(GetMyGifticonOrdersResult result) {
        List<MyGifticonOrderItemResponse> items = result.items().stream()
                .map(item -> new MyGifticonOrderItemResponse(
                        item.orderId(),
                        item.goodsName(),
                        item.brandName(),
                        item.productImageUrl(),
                        item.cashPrice(),
                        item.expiryDate(),
                        item.daysRemaining(),
                        item.statusLabel(),
                        item.orderStatus(),
                        item.createdAt()
                ))
                .toList();

        return new GetMyGifticonOrdersResponse(
                result.availableCount(),
                result.completedOrExpiredCount(),
                result.hasNext(),
                result.nextCursor(),
                items
        );
    }
}
