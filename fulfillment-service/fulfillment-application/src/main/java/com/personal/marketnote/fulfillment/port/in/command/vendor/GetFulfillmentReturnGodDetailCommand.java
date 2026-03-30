package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentReturnGodDetailCommand(
        String customerCode,
        String accessToken,
        String startDate,
        String endDate,
        String rtnSlipNoList,
        String whCd
) {
    public static GetFulfillmentReturnGodDetailCommand of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String rtnSlipNoList,
            String whCd
    ) {
        return new GetFulfillmentReturnGodDetailCommand(
                customerCode, accessToken, startDate, endDate, rtnSlipNoList, whCd
        );
    }
}
