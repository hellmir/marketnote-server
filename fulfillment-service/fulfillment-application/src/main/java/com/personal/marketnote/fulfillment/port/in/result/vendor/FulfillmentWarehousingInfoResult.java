package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

import lombok.Builder;

@Builder
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
}
