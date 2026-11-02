package com.personal.marketnote.fulfillment.port.in.result;

public record InternalReturnGodDetailGoodsResult(
        String customerProductCode,
        String productName,
        String returnProductCheckStatus,
        String returnProductCheckStatusName
) {
    public static InternalReturnGodDetailGoodsResult of(
            String customerProductCode,
            String productName,
            String returnProductCheckStatus,
            String returnProductCheckStatusName
    ) {
        return new InternalReturnGodDetailGoodsResult(
                customerProductCode, productName, returnProductCheckStatus, returnProductCheckStatusName
        );
    }
}
