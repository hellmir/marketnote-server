package com.personal.marketnote.fulfillment.port.in.result.vendor;

import lombok.Builder;

@Builder
public record CancelFulfillmentDeliveryItemResult(
        String fulfillmentSlipNumber,
        String orderNumber,
        String message,
        String code,
        Object outOfStockGoodsDetail
) {
}
