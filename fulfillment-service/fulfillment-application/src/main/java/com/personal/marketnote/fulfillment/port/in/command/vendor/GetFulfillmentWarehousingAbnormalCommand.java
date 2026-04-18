package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentWarehousingAbnormalCommand(
        String customerCode,
        String accessToken,
        String warehouseCode,
        String slipNumber
) {
    public static GetFulfillmentWarehousingAbnormalCommand of(
            String customerCode,
            String accessToken,
            String warehouseCode,
            String slipNumber
    ) {
        return new GetFulfillmentWarehousingAbnormalCommand(customerCode, accessToken, warehouseCode, slipNumber);
    }
}
