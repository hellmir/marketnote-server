package com.personal.marketnote.common.kafka.event;

import java.util.List;

public record OrderPurchaseConfirmedEvent(
        Long orderId,
        Long buyerId,
        List<Long> sharerIds
) {
}
