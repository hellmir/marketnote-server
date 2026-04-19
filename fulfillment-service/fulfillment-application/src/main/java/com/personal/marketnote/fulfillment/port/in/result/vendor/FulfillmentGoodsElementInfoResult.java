package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record FulfillmentGoodsElementInfoResult(
        String productCode,
        String customerProductCode,
        String productName,
        String useYn,
        List<FulfillmentGoodsElementItemResult> elementList
) {
    public static FulfillmentGoodsElementInfoResult of(
            String productCode,
            String customerProductCode,
            String productName,
            String useYn,
            List<FulfillmentGoodsElementItemResult> elementList
    ) {
        return new FulfillmentGoodsElementInfoResult(
                productCode,
                customerProductCode,
                productName,
                useYn,
                elementList
        );
    }
}
