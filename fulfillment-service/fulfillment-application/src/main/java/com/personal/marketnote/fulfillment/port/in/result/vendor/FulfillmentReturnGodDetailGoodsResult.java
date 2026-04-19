package com.personal.marketnote.fulfillment.port.in.result.vendor;

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
    public static FulfillmentReturnGodDetailGoodsResult of(
            String customerProductCode,
            String productName,
            String manufacturingDate,
            String expirationDate,
            String inboundQuantity,
            String remark,
            String returnProductCheckStatus,
            String returnProductCheckStatusName
    ) {
        return new FulfillmentReturnGodDetailGoodsResult(
                customerProductCode, productName, manufacturingDate, expirationDate, inboundQuantity, remark, returnProductCheckStatus, returnProductCheckStatusName
        );
    }
}
