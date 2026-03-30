package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record SyncFulfillmentStockCommand(
        String customerCode,
        List<Long> productIds
) {
    public static SyncFulfillmentStockCommand of(
            String customerCode,
            List<Long> productIds
    ) {
        return new SyncFulfillmentStockCommand(customerCode, productIds);
    }
}
