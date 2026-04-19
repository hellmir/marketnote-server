package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record SyncFulfillmentAllStockCommand(
        String customerCode,
        String warehouseCode
) {
    public static SyncFulfillmentAllStockCommand of(
            String customerCode
    ) {
        return new SyncFulfillmentAllStockCommand(customerCode, null);
    }

    public static SyncFulfillmentAllStockCommand of(
            String customerCode,
            String warehouseCode
    ) {
        return new SyncFulfillmentAllStockCommand(customerCode, warehouseCode);
    }
}
