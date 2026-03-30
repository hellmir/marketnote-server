package com.personal.marketnote.fulfillment.port.in.command.vendor;

public record GetFulfillmentGoodsElementsCommand(
        String customerCode,
        String accessToken
) {
    public static GetFulfillmentGoodsElementsCommand of(String customerCode, String accessToken) {
        return new GetFulfillmentGoodsElementsCommand(customerCode, accessToken);
    }
}
