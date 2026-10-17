package com.personal.marketnote.commerce.port.out.event;

import com.personal.marketnote.common.kafka.event.InventoryChangeAction;

public interface PublishInventoryEventPort {

    void publishInventoryChangedEvent(Long pricePolicyId, Long productId, Integer stockQuantity,
                                      InventoryChangeAction action);

    void publishStockReservedEvent(Long pricePolicyId, Long productId, Integer reservedQuantity, Long orderId);

    void publishStockReleasedEvent(Long pricePolicyId, Long productId, Integer releasedQuantity,
                                   String reason, Long orderId);
}
