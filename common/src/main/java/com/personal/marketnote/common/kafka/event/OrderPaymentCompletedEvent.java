package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderPaymentCompletedEvent(
    Long orderId,
    Long buyerId,
    Long totalAmount,
    Long pointAmount,
    String recipientName,
    String recipientPhoneNumber,
    String zipCode,
    String address,
    String addressDetail,
    String deliveryRequestMessage,
    List<OrderProductItem> orderProducts,
    Long totalAccumulatedPoint
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderProductItem(
        Long pricePolicyId,
        Long productId,
        UUID productKey,
        UUID sharerKey,
        Integer quantity,
        Long unitAmount
    ) {
    }
}
