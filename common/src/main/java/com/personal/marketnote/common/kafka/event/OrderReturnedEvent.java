package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderReturnedEvent(
        Long orderId,
        String orderKey,
        Long buyerId,
        Long returnAmount,
        Long paymentAmount,
        Long pointAmount,
        Long shippingFee,
        boolean isFullReturn,
        Long returnShippingFee,
        List<OrderProductItem> returnProducts,
        String returnId
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
