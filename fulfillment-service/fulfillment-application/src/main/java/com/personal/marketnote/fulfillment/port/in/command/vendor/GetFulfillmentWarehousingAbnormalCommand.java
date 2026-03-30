package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentWarehousingAbnormalCommand(
        String customerCode,
        String accessToken,
        String whCd,
        String slipNo
) {
    public static GetFulfillmentWarehousingAbnormalCommand of(
            String customerCode,
            String accessToken,
            String whCd,
            String slipNo
    ) {
        return new GetFulfillmentWarehousingAbnormalCommand(customerCode, accessToken, whCd, slipNo);
    }
}
