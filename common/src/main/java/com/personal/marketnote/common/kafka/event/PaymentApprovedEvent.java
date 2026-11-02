package com.personal.marketnote.common.kafka.event;

public record PaymentApprovedEvent(
        Long orderId,
        String orderKey,
        Long paymentAmount
) {
}
