package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

import lombok.Builder;

@Builder
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
}
