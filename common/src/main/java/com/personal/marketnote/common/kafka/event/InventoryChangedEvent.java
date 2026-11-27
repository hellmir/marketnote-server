package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryChangedEvent(
    Long pricePolicyId,
    Long productId,
    Integer stockQuantity,
    InventoryChangeAction action
) {
}
