package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record RegisterFulfillmentGoodsItemResult(
        String message,
        String code,
        String customerProductCode
) {
    public static RegisterFulfillmentGoodsItemResult of(String message, String code, String customerProductCode) {
        return new RegisterFulfillmentGoodsItemResult(message, code, customerProductCode);
    }
}
