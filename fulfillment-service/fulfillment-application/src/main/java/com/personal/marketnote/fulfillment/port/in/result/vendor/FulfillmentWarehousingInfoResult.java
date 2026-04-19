package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record FulfillmentWarehousingInfoResult(
        String orderDate,
        String warehouseCode,
        String warehouseName,
        String orderNumber,
        String slipNumber,
        String customerCode,
        String customerName,
        String supplierCode,
        String customerSupplierCode,
        Integer sku,
        String supplierName,
        Integer orderQuantity,
        Integer inboundQuantity,
        String inboundMethod,
        String inboundMethodName,
        String courierCompany,
        String trackingNumber,
        String workStatus,
        String workStatusName,
        String emergencyYn,
        String remark,
        List<Object> goodsSerialNo
) {
    public static FulfillmentWarehousingInfoResult of(
            String orderDate,
            String warehouseCode,
            String warehouseName,
            String orderNumber,
            String slipNumber,
            String customerCode,
            String customerName,
            String supplierCode,
            String customerSupplierCode,
            Integer sku,
            String supplierName,
            Integer orderQuantity,
            Integer inboundQuantity,
            String inboundMethod,
            String inboundMethodName,
            String courierCompany,
            String trackingNumber,
            String workStatus,
            String workStatusName,
            String emergencyYn,
            String remark,
            List<Object> goodsSerialNo
    ) {
        return new FulfillmentWarehousingInfoResult(
                orderDate,
                warehouseCode,
                warehouseName,
                orderNumber,
                slipNumber,
                customerCode,
                customerName,
                supplierCode,
                customerSupplierCode,
                sku,
                supplierName,
                orderQuantity,
                inboundQuantity,
                inboundMethod,
                inboundMethodName,
                courierCompany,
                trackingNumber,
                workStatus,
                workStatusName,
                emergencyYn,
                remark,
                goodsSerialNo
        );
    }
}
