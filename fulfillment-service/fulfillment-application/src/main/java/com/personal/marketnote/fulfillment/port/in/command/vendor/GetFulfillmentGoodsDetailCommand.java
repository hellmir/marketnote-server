package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentGoodsDetailCommand(
        String customerCode,
        String accessToken,
        String productName
) {
    public static GetFulfillmentGoodsDetailCommand of(
            String customerCode,
            String accessToken,
            String productName
    ) {
        return new GetFulfillmentGoodsDetailCommand(customerCode, accessToken, productName);
    }
}
