package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record FulfillmentGoodsElementItemResult(
        String godCd,
        String cstGodCd,
        String godBarcd,
        String godNm,
        String godType,
        String godTypeNm,
        Integer qty
) {
    public static FulfillmentGoodsElementItemResult of(
            String godCd,
            String cstGodCd,
            String godBarcd,
            String godNm,
            String godType,
            String godTypeNm,
            Integer qty
    ) {
        return new FulfillmentGoodsElementItemResult(
                godCd,
                cstGodCd,
                godBarcd,
                godNm,
                godType,
                godTypeNm,
                qty
        );
    }
}
