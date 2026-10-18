package com.personal.marketnote.common.kafka.event;

import java.util.List;
import java.util.UUID;

public record FulfillmentInventorySyncedEvent(
        List<InventoryItem> inventories
) {

    public record InventoryItem(
            Long productId,
            UUID productKey,
            Integer stock
    ) {
    }
}
