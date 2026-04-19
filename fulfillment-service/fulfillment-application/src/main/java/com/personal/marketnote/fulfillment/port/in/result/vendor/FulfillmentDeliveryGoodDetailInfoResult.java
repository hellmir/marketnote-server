package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.math.BigDecimal;

public record FulfillmentDeliveryGoodDetailInfoResult(
        String releaseDate,
        String slipNumber,
        String releaseOrderSlipNumber,
        String orderNumber,
        String productOrderNo,
        String orderDivision,
        String invoiceNumber,
        String sellerChannel,
        String recipientName,
        String productCode,
        String customerProductCode,
        String productName,
        Integer releaseQuantity,
        BigDecimal markedPrAmount,
        BigDecimal sellingPrAmount,
        BigDecimal dcAmount,
        BigDecimal sellerDcAmount,
        BigDecimal naverDcAmount
) {
    public static FulfillmentDeliveryGoodDetailInfoResult of(
            String releaseDate,
            String slipNumber,
            String releaseOrderSlipNumber,
            String orderNumber,
            String productOrderNo,
            String orderDivision,
            String invoiceNumber,
            String sellerChannel,
            String recipientName,
            String productCode,
            String customerProductCode,
            String productName,
            Integer releaseQuantity,
            BigDecimal markedPrAmount,
            BigDecimal sellingPrAmount,
            BigDecimal dcAmount,
            BigDecimal sellerDcAmount,
            BigDecimal naverDcAmount
    ) {
        return new FulfillmentDeliveryGoodDetailInfoResult(
                releaseDate, slipNumber, releaseOrderSlipNumber, orderNumber, productOrderNo,
                orderDivision, invoiceNumber, sellerChannel, recipientName, productCode,
                customerProductCode, productName, releaseQuantity, markedPrAmount, sellingPrAmount,
                dcAmount, sellerDcAmount, naverDcAmount
        );
    }
}
