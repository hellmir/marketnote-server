package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCancelledEvent(
        Long orderId,
        String orderKey,
        Long buyerId,
        Long cancelAmount,
        Long paymentAmount,
        Long pointAmount,
        Long shippingFee,
        boolean isFullCancel,
        Long alreadyRefunded,
        List<OrderProductItem> orderProducts,
        List<OrderProductItem> cancelProducts,
        String cancelId
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderProductItem(
            Long pricePolicyId,
            UUID sharerKey,
            Integer quantity,
            Long unitAmount
    ) {
    }
}
