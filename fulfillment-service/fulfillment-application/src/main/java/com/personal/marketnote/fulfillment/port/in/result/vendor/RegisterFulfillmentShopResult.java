package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record RegisterFulfillmentShopResult(
        String msg,
        String code,
        String shopCd
) {
    public static RegisterFulfillmentShopResult of(String msg, String code, String shopCd) {
        return new RegisterFulfillmentShopResult(msg, code, shopCd);
    }
}
