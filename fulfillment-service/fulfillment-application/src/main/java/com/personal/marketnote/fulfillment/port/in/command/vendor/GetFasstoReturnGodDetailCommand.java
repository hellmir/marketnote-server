package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFasstoReturnGodDetailCommand(
        String customerCode,
        String accessToken,
        String startDate,
        String endDate,
        String rtnSlipNoList,
        String whCd
) {
    public static GetFasstoReturnGodDetailCommand of(
            String customerCode,
            String accessToken,
            String startDate,
            String endDate,
            String rtnSlipNoList,
            String whCd
    ) {
        return new GetFasstoReturnGodDetailCommand(
                customerCode, accessToken, startDate, endDate, rtnSlipNoList, whCd
        );
    }
}
