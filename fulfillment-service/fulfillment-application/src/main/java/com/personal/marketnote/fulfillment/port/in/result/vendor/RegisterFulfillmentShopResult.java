package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record RegisterFulfillmentShopResult(
        String message,
        String code,
        String shopCode
) {
    public static RegisterFulfillmentShopResult of(String message, String code, String shopCode) {
        return new RegisterFulfillmentShopResult(message, code, shopCode);
    }
}
