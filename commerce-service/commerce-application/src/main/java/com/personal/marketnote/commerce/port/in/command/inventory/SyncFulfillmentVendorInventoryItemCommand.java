package com.personal.marketnote.commerce.port.in.command.inventory;

import java.util.UUID;

public record SyncFulfillmentVendorInventoryItemCommand(
        UUID productKey,
        Integer stock
) {
    public static SyncFulfillmentVendorInventoryItemCommand of(
            UUID productKey,
            Integer stock
    ) {
        return new SyncFulfillmentVendorInventoryItemCommand(productKey, stock);
    }
}
