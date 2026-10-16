package com.personal.marketnote.commerce.port.in.command.saga;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record OrderPaymentCompletedSagaContext(
        Long orderId,
        Long buyerId,
        Long totalAmount,
        Long pointAmount,
        Long totalAccumulatedPoint,
        String recipientName,
        String recipientPhoneNumber,
        String zipCode,
        String address,
        String addressDetail,
        String deliveryRequestMessage,
        List<OrderProductItem> orderProducts
) {
    public record OrderProductItem(
            Long pricePolicyId,
            Long productId,
            UUID sharerKey,
            Integer quantity,
            Long unitAmount
    ) {
    }
}
