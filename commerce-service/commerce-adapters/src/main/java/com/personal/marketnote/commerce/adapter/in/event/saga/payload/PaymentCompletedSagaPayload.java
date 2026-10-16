package com.personal.marketnote.commerce.adapter.in.event.saga.payload;

import java.util.List;

public record PaymentCompletedSagaPayload(
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
        List<SagaOrderProductItem> orderProducts
) {
}
