package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentWarehousingInspecDetailCommand(
        String customerCode,
        String accessToken,
        String slipNumber,
        String warehouseCode
) {
    public static GetFulfillmentWarehousingInspecDetailCommand of(
            String customerCode,
            String accessToken,
            String slipNumber,
            String warehouseCode
    ) {
        return new GetFulfillmentWarehousingInspecDetailCommand(customerCode, accessToken, slipNumber, warehouseCode);
    }
}
