package com.personal.marketnote.reward.adapter.in.event.saga.payload;

public record OrderPointSagaPayload(
        Long orderId,
        Long buyerId,
        Long pointAmount
) {
}
