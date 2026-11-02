package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

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
    public static FulfillmentStockInfoResult of(
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
        return new FulfillmentStockInfoResult(
                warehouseCode,
                productCode,
                customerProductCode,
                productName,
                distributionTermDate,
                distributionTermManagementYn,
                productBarcode,
                stockQuantity,
                defectiveStockQuantity,
                availableStockQuantity,
                customerSupplierCode,
                supplierName,
                giftDivision,
                goodsSerialNumbers,
                slipNumber
        );
    }
}
