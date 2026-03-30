package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record RegisterFulfillmentGoodsItemResult(
        String msg,
        String code,
        String cstGodCd
) {
    public static RegisterFulfillmentGoodsItemResult of(String msg, String code, String cstGodCd) {
        return new RegisterFulfillmentGoodsItemResult(msg, code, cstGodCd);
    }
}
