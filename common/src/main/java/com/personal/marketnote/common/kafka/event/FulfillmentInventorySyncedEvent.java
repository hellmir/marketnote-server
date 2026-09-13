package com.personal.marketnote.common.kafka.event;

import java.util.List;

public record FulfillmentInventorySyncedEvent(
        List<InventoryItem> inventories
) {

    public record InventoryItem(
            Long productId,
            Integer stock
    ) {
    }
}
