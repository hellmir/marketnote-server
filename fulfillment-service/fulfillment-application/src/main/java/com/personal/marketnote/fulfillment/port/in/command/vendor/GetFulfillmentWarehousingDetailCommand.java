package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentWarehousingDetailCommand(
        String customerCode,
        String accessToken,
        String slipNumber,
        String orderNumber
) {
    public static GetFulfillmentWarehousingDetailCommand of(
            String customerCode,
            String accessToken,
            String slipNumber,
            String orderNumber
    ) {
        return new GetFulfillmentWarehousingDetailCommand(customerCode, accessToken, slipNumber, orderNumber);
    }
}
