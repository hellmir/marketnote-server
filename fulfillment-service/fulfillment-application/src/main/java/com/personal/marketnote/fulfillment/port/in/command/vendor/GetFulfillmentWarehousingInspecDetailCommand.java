package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentWarehousingInspecDetailCommand(
        String customerCode,
        String accessToken,
        String slipNo,
        String whCd
) {
    public static GetFulfillmentWarehousingInspecDetailCommand of(
            String customerCode,
            String accessToken,
            String slipNo,
            String whCd
    ) {
        return new GetFulfillmentWarehousingInspecDetailCommand(customerCode, accessToken, slipNo, whCd);
    }
}
