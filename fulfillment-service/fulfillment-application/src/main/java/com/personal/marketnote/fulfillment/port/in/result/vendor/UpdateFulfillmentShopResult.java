package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record UpdateFulfillmentShopResult(
        String message,
        String code,
        String shopCode
) {
    public static UpdateFulfillmentShopResult of(String message, String code, String shopCode) {
        return new UpdateFulfillmentShopResult(message, code, shopCode);
    }
}
