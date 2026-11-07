package com.personal.marketnote.fulfillment.port.in.result.vendor;

import lombok.Builder;

@Builder
public record FulfillmentReturnGodDetailGoodsResult(
        String customerProductCode,
        String productName,
        String manufacturingDate,
        String expirationDate,
        String inboundQuantity,
        String remark,
        String returnProductCheckStatus,
        String returnProductCheckStatusName
) {
}
