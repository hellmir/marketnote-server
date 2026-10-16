package com.personal.marketnote.reward.adapter.in.event.saga.payload;

public record ProductPointSagaPayload(
        Long orderId,
        Long buyerId,
        Long totalAmount,
        Long totalAccumulatedPoint
) {
}
