package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;
import java.util.UUID;

public record SyncFulfillmentStockCommand(
        String customerCode,
        List<Long> productIds,
        List<UUID> productKeys
) {
    public static SyncFulfillmentStockCommand of(
            String customerCode,
            List<Long> productIds
    ) {
        return new SyncFulfillmentStockCommand(customerCode, productIds, null);
    }

    public static SyncFulfillmentStockCommand ofProductKeys(
            String customerCode,
            List<UUID> productKeys
    ) {
        return new SyncFulfillmentStockCommand(customerCode, null, productKeys);
    }
}
