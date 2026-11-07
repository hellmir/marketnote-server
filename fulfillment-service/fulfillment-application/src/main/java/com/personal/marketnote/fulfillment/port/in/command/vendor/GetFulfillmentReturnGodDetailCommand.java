package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

@Builder
public record GetFulfillmentReturnGodDetailCommand(
        String customerCode,
        String accessToken,
        String startDate,
        String endDate,
        String returnSlipNumbers,
        String warehouseCode
) {
    public static GetFulfillmentReturnGodDetailCommand forReturnSlipNumbers(
            String customerCode,
            String accessToken,
            String returnSlipNumbers
    ) {
        return GetFulfillmentReturnGodDetailCommand.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .returnSlipNumbers(returnSlipNumbers)
                .build();
    }
}
