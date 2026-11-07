package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

@Builder
public record GetFulfillmentWarehousingCommand(
        String customerCode,
        String accessToken,
        String startDate,
        String endDate,
        String warehousingMethod,
        String orderNumber,
        String workStatus
) {
    public static GetFulfillmentWarehousingCommand of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate
    ) {
        return GetFulfillmentWarehousingCommand.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .startDate(startDate)
                .endDate(endDate)
                .warehousingMethod(null)
                .orderNumber(null)
                .workStatus(null)
                .build();
    }

}
