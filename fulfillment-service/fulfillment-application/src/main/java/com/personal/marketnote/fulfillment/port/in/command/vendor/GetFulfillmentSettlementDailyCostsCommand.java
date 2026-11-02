package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentSettlementDailyCostsCommand(
        String yearMonth,
        String warehouseCode,
        String customerCode,
        String accessToken
) {
    public static GetFulfillmentSettlementDailyCostsCommand of(
            String yearMonth,
            String warehouseCode,
            String customerCode,
            String accessToken
    ) {
        return new GetFulfillmentSettlementDailyCostsCommand(yearMonth, warehouseCode, customerCode, accessToken);
    }
}
