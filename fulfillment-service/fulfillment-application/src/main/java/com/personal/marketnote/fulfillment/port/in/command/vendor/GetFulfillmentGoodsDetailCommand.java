package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentGoodsDetailCommand(
        String customerCode,
        String accessToken,
        String godNm
) {
    public static GetFulfillmentGoodsDetailCommand of(
            String customerCode,
            String accessToken,
            String godNm
    ) {
        return new GetFulfillmentGoodsDetailCommand(customerCode, accessToken, godNm);
    }
}
