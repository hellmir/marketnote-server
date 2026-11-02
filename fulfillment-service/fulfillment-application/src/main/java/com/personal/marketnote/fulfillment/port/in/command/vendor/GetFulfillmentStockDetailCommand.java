package com.personal.marketnote.fulfillment.port.in.command.vendor;

import java.util.UUID;

public record GetFulfillmentStockDetailCommand(
        String customerCode,
        String accessToken,
        String customerProductCode,
        UUID productKey,
        String outOfStockYn
) {
    public static GetFulfillmentStockDetailCommand of(
            String customerCode,
            String accessToken,
            String customerProductCode,
            String outOfStockYn
    ) {
        return new GetFulfillmentStockDetailCommand(
                customerCode, accessToken, customerProductCode, null, outOfStockYn
        );
    }

    public static GetFulfillmentStockDetailCommand of(
            String customerCode,
            String accessToken,
            String customerProductCode,
            UUID productKey,
            String outOfStockYn
    ) {
        return new GetFulfillmentStockDetailCommand(
                customerCode, accessToken, customerProductCode, productKey, outOfStockYn
        );
    }
}
