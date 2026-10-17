package com.personal.marketnote.common.kafka.event;

public record StockReleasedEvent(
        Long pricePolicyId,
        Long productId,
        Integer releasedQuantity,
        String reason,
        Long orderId
) {
}
