package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StockReservedEvent(
        Long pricePolicyId,
        Long productId,
        Integer reservedQuantity,
        Long orderId
) {
}
