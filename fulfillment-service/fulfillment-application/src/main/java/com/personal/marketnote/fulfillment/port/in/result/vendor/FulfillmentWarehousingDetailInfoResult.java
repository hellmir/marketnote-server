package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

import lombok.Builder;

@Builder
public record FulfillmentWarehousingDetailInfoResult(
        String orderDate,
        String warehouseCode,
        String warehouseName,
        String slipNumber,
        String orderNumber,
        String customerCode,
        String customerName,
        String supplierCode,
        String supplierName,
        String customerSupplierCode,
        Integer sku,
        Integer orderQuantity,
        Integer inboundQuantity,
        Integer targetQuantity,
        String inboundMethod,
        String inboundMethodName,
        String courierCompany,
        String trackingNumber,
        String workStatus,
        String workStatusName,
        String emergencyYn,
        String remark,
        List<FulfillmentWarehousingDetailGoodsInfoResult> goods
) {
}
