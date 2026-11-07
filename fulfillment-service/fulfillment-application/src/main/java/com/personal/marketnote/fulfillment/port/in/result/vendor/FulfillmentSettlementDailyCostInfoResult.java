package com.personal.marketnote.fulfillment.port.in.result.vendor;

import lombok.Builder;

@Builder
public record FulfillmentSettlementDailyCostInfoResult(
        String closeDate,
        String warehouseCode,
        String customerCode,
        String inboundAmount,
        String outboundAmount,
        String outboundCarAmount,
        String outboundAirAmount,
        String storageAmount,
        String returnAmount,
        String cashAmount,
        String foundationAmount,
        String otherAmount,
        String totalAmount
) {
}
