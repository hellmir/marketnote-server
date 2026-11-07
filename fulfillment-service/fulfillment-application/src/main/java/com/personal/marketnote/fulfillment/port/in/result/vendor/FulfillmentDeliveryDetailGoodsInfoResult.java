package com.personal.marketnote.fulfillment.port.in.result.vendor;

import lombok.Builder;

@Builder
public record FulfillmentDeliveryDetailGoodsInfoResult(
        String orderDate,
        String warehouseCode,
        String slipNumber,
        String customerCode,
        String shopCode,
        String supplierCode,
        String productCode,
        String customerProductCode,
        String productName,
        String originalProductCode,
        String productType,
        String productTypeName,
        String expirationDate,
        Integer stockQuantity,
        Integer orderQuantity,
        Integer additionalProductOrderQuantity,
        Integer orderQuantitySum,
        String giftDivision,
        String additionalType,
        String emergencyYn
) {
}
