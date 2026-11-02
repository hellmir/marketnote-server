package com.personal.marketnote.common.kafka.event;

public record PaymentFailedEvent(
        Long orderId,
        String orderKey,
        String resultCode,
        String resultMessage
) {
}
