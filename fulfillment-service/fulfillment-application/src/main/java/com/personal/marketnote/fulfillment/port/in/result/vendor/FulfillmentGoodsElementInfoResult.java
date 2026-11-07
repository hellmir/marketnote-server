package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

import lombok.Builder;

@Builder
public record FulfillmentGoodsElementInfoResult(
        String productCode,
        String customerProductCode,
        String productName,
        String useYn,
        List<FulfillmentGoodsElementItemResult> elementList
) {
}
