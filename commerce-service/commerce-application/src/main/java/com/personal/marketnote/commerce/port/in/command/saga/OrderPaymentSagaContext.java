package com.personal.marketnote.commerce.port.in.command.saga;

import java.util.List;
import java.util.UUID;

public record OrderPaymentSagaContext(
        Long orderId,
        String orderKey,
        Long buyerId,
        Long paymentAmount,
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
            UUID productKey,
            UUID sharerKey,
            Integer quantity,
            Long unitAmount
    ) {
    }
}
