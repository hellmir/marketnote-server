package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record UpdateFulfillmentShopResult(
        String msg,
        String code,
        String shopCd
) {
    public static UpdateFulfillmentShopResult of(String msg, String code, String shopCd) {
        return new UpdateFulfillmentShopResult(msg, code, shopCd);
    }
}
