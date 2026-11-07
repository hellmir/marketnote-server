package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
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
}
