package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentReturnGodDetailCommand(
        String customerCode,
        String accessToken,
        String startDate,
        String endDate,
        String returnSlipNumbers,
        String warehouseCode
) {
    public static GetFulfillmentReturnGodDetailCommand of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String returnSlipNumbers,
            String warehouseCode
    ) {
        return new GetFulfillmentReturnGodDetailCommand(
                customerCode, accessToken, startDate, endDate, returnSlipNumbers, warehouseCode
        );
    }
}
