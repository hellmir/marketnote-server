package com.personal.marketnote.commerce.port.in.command.inventory;

import java.util.UUID;

public record SyncFulfillmentVendorInventoryItemCommand(
        Long productId,
        UUID productKey,
        Integer stock
) {
    public static SyncFulfillmentVendorInventoryItemCommand of(
            Long productId,
            Integer stock
    ) {
        return new SyncFulfillmentVendorInventoryItemCommand(productId, null, stock);
    }

    public static SyncFulfillmentVendorInventoryItemCommand of(
            Long productId,
            UUID productKey,
            Integer stock
    ) {
        return new SyncFulfillmentVendorInventoryItemCommand(productId, productKey, stock);
    }
}
