package com.personal.marketnote.reward.adapter.in.event.saga.payload;

import java.util.List;
import java.util.UUID;

public record SharedPointSagaPayload(
        Long orderId,
        Long buyerId,
        Long totalAmount,
        List<UUID> sharerKeys
) {
}
