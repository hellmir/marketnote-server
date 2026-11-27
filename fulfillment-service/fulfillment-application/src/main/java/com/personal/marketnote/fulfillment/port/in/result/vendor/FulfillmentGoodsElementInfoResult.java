package com.personal.marketnote.fulfillment.port.in.result.vendor;

import lombok.Builder;

import java.util.List;

@Builder
public record FulfillmentGoodsElementInfoResult(
    String productCode,
    String customerProductCode,
    String productName,
    String useYn,
    List<FulfillmentGoodsElementItemResult> elementList
) {
}
