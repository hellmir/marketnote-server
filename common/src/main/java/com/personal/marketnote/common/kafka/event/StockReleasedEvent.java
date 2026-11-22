package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StockReleasedEvent(
        Long pricePolicyId,
        Long productId,
        Integer releasedQuantity,
        String reason,
        Long orderId
) {
}
