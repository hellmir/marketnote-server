package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentGoodsCommand(
        String customerCode,
        String accessToken
) {
    public static GetFulfillmentGoodsCommand of(String customerCode, String accessToken) {
        return new GetFulfillmentGoodsCommand(customerCode, accessToken);
    }
}
