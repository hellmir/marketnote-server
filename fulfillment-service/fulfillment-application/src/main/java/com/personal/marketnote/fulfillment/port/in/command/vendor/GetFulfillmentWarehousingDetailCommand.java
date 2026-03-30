package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentWarehousingDetailCommand(
        String customerCode,
        String accessToken,
        String slipNo,
        String ordNo
) {
    public static GetFulfillmentWarehousingDetailCommand of(
            String customerCode,
            String accessToken,
            String slipNo,
            String ordNo
    ) {
        return new GetFulfillmentWarehousingDetailCommand(customerCode, accessToken, slipNo, ordNo);
    }
}
