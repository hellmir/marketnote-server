package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record FulfillmentGoodsElementInfoResult(
        String godCd,
        String cstGodCd,
        String godNm,
        String useYn,
        List<FulfillmentGoodsElementItemResult> elementList
) {
    public static FulfillmentGoodsElementInfoResult of(
            String godCd,
            String cstGodCd,
            String godNm,
            String useYn,
            List<FulfillmentGoodsElementItemResult> elementList
    ) {
        return new FulfillmentGoodsElementInfoResult(
                godCd,
                cstGodCd,
                godNm,
                useYn,
                elementList
        );
    }
}
