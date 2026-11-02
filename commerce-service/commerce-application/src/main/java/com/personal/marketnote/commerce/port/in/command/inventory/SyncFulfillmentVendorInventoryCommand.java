package com.personal.marketnote.commerce.port.in.command.inventory;

import java.util.List;

public record SyncFulfillmentVendorInventoryCommand(
        List<SyncFulfillmentVendorInventoryItemCommand> inventories
) {
    public static final int MAX_INVENTORIES_SIZE = 1000;

    public static SyncFulfillmentVendorInventoryCommand of(
            List<SyncFulfillmentVendorInventoryItemCommand> inventories
    ) {
        return new SyncFulfillmentVendorInventoryCommand(inventories);
    }
}
