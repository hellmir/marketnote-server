package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FulfillmentInventorySyncedEvent(
        List<InventoryItem> inventories
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InventoryItem(
            UUID productKey,
            Integer stock
    ) {
    }
}
