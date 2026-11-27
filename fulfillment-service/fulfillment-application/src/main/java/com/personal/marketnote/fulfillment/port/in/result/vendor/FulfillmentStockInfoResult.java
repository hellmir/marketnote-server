package com.personal.marketnote.fulfillment.port.in.result.vendor;

import lombok.Builder;

import java.util.List;

@Builder
public record FulfillmentStockInfoResult(
    String warehouseCode,
    String productCode,
    String customerProductCode,
    String productName,
    String distributionTermDate,
    String distributionTermManagementYn,
    String productBarcode,
    Integer stockQuantity,
    Integer defectiveStockQuantity,
    Integer availableStockQuantity,
    String customerSupplierCode,
    String supplierName,
    String giftDivision,
    List<Object> goodsSerialNumbers,
    String slipNumber
) {
}
