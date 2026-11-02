package com.personal.marketnote.commerce.port.out.inventory;

import com.personal.marketnote.commerce.domain.inventory.InventoryRestorationHistories;

public interface SaveInventoryRestorationHistoryPort {
    void save(InventoryRestorationHistories inventoryRestorationHistories);
}
