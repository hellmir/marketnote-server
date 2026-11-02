package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentDeliveryOutOrdGoodsDetailCommand(
        String customerCode,
        String accessToken,
        String releaseOrderSlipNumber
) {
    public static GetFulfillmentDeliveryOutOrdGoodsDetailCommand of(
            String customerCode,
            String accessToken,
            String releaseOrderSlipNumber
    ) {
        return new GetFulfillmentDeliveryOutOrdGoodsDetailCommand(customerCode, accessToken, releaseOrderSlipNumber);
    }
}
