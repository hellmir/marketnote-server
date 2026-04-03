package com.personal.marketnote.commerce.port.out.event;

import com.personal.marketnote.common.kafka.event.InventoryChangeAction;

public interface PublishInventoryEventPort {

    void publishInventoryChangedEvent(Long pricePolicyId, Long productId, Integer stockQuantity,
                                      InventoryChangeAction action);
}
