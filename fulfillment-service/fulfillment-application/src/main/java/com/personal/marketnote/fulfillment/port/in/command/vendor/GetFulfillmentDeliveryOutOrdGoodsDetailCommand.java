package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentDeliveryOutOrdGoodsDetailCommand(
        String customerCode,
        String accessToken,
        String outOrdSlipNo
) {
    public static GetFulfillmentDeliveryOutOrdGoodsDetailCommand of(
            String customerCode,
            String accessToken,
            String outOrdSlipNo
    ) {
        return new GetFulfillmentDeliveryOutOrdGoodsDetailCommand(customerCode, accessToken, outOrdSlipNo);
    }
}
