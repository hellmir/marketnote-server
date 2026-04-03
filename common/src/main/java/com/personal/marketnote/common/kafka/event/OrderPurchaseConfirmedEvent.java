package com.personal.marketnote.common.kafka.event;

import java.util.List;
import java.util.UUID;

public record OrderPurchaseConfirmedEvent(
        Long orderId,
        Long buyerId,
        List<UUID> sharerKeys
) {
}
