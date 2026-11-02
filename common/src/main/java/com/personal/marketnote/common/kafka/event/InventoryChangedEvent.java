package com.personal.marketnote.common.kafka.event;

public record InventoryChangedEvent(
        Long pricePolicyId,
        Long productId,
        Integer stockQuantity,
        InventoryChangeAction action
) {
}
