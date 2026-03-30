package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentWarehousingCommand(
        String customerCode,
        String accessToken,
        String startDate,
        String endDate,
        String inWay,
        String ordNo,
        String wrkStat
) {
    public static GetFulfillmentWarehousingCommand of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate
    ) {
        return new GetFulfillmentWarehousingCommand(customerCode, accessToken, startDate, endDate, null, null, null);
    }

    public static GetFulfillmentWarehousingCommand of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String inWay,
            String ordNo,
            String wrkStat
    ) {
        return new GetFulfillmentWarehousingCommand(customerCode, accessToken, startDate, endDate, inWay, ordNo, wrkStat);
    }
}
