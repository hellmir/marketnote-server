package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record FulfillmentWarehousingInspecDetailInfoResult(
        String orderDate,
        String warehouseCode,
        String warehouseName,
        String slipNumber,
        String customerCode,
        String customerName,
        String supplierCode,
        String supplierName,
        String inboundMethod,
        String inboundMethodName,
        String productCode,
        Integer orderQuantity,
        Integer totalInboundQuantity,
        String courierCompany,
        String trackingNumber,
        String workStatus,
        String workStatusName,
        String remark,
        List<Object> goods,
        List<Object> goodsSerialNo,
        String externalProductImageUrl,
        String expirationDate
) {
    public static FulfillmentWarehousingInspecDetailInfoResult of(
            String orderDate,
            String warehouseCode,
            String warehouseName,
            String slipNumber,
            String customerCode,
            String customerName,
            String supplierCode,
            String supplierName,
            String inboundMethod,
            String inboundMethodName,
            String productCode,
            Integer orderQuantity,
            Integer totalInboundQuantity,
            String courierCompany,
            String trackingNumber,
            String workStatus,
            String workStatusName,
            String remark,
            List<Object> goods,
            List<Object> goodsSerialNo,
            String externalProductImageUrl,
            String expirationDate
    ) {
        return new FulfillmentWarehousingInspecDetailInfoResult(
                orderDate,
                warehouseCode,
                warehouseName,
                slipNumber,
                customerCode,
                customerName,
                supplierCode,
                supplierName,
                inboundMethod,
                inboundMethodName,
                productCode,
                orderQuantity,
                totalInboundQuantity,
                courierCompany,
                trackingNumber,
                workStatus,
                workStatusName,
                remark,
                goods,
                goodsSerialNo,
                externalProductImageUrl,
                expirationDate
        );
    }
}
