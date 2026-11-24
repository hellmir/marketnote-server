package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.List;

public record SyncFulfillmentStockCommand(
        String customerCode,
        List<String> fasstoProductCodes
) {
    public static SyncFulfillmentStockCommand of(
            String customerCode,
            List<String> fasstoProductCodes
    ) {
        return new SyncFulfillmentStockCommand(customerCode, fasstoProductCodes);
    }
}
