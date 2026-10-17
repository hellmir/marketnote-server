package com.personal.marketnote.common.kafka.event;

public record StockReservedEvent(
        Long pricePolicyId,
        Long productId,
        Integer reservedQuantity,
        Long orderId
) {
}
