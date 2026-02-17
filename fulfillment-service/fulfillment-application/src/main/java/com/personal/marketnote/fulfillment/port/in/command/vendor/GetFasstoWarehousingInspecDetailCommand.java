package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFasstoWarehousingInspecDetailCommand(
        String customerCode,
        String accessToken,
        String slipNo,
        String whCd
) {
    public static GetFasstoWarehousingInspecDetailCommand of(
            String customerCode,
            String accessToken,
            String slipNo,
            String whCd
    ) {
        return new GetFasstoWarehousingInspecDetailCommand(customerCode, accessToken, slipNo, whCd);
    }
}
