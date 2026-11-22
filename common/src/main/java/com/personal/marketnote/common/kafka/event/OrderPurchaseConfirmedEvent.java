package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderPurchaseConfirmedEvent(
        Long orderId,
        Long buyerId,
        List<UUID> sharerKeys,
        boolean isAutoConfirmed,
        List<OrderProductItem> orderProducts
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderProductItem(
            Long pricePolicyId,
            Long productId,
            Integer quantity
    ) {
    }
}
