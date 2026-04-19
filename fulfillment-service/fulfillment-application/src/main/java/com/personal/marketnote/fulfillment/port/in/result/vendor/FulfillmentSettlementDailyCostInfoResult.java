package com.personal.marketnote.fulfillment.port.in.result.vendor;

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
    public static FulfillmentSettlementDailyCostInfoResult of(
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
        return new FulfillmentSettlementDailyCostInfoResult(
                closeDate,
                warehouseCode,
                customerCode,
                inboundAmount,
                outboundAmount,
                outboundCarAmount,
                outboundAirAmount,
                storageAmount,
                returnAmount,
                cashAmount,
                foundationAmount,
                otherAmount,
                totalAmount
        );
    }
}
