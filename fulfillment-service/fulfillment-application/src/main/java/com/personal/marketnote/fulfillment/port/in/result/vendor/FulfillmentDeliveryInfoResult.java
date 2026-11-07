package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

import lombok.Builder;

@Builder
public record FulfillmentDeliveryInfoResult(
        String releaseDate,
        String orderDate,
        String warehouseCode,
        String warehouseName,
        String slipNumber,
        String customerCode,
        String customerName,
        String shopCode,
        String mappingSlipNumber,
        String shopName,
        Integer sku,
        Integer orderQuantity,
        Integer additionalProductOrderQuantity,
        String releaseDivision,
        String releaseDivisionName,
        String customerShopCode,
        String orderNumber,
        Integer orderSequence,
        String shippingRequestTerm,
        String salesChannel,
        String releaseMethod,
        String orderDivision,
        String releaseMethodName,
        String workStatus,
        String workStatusName,
        String invoiceNumber,
        String courierName,
        String courierCode,
        String recipientName,
        List<Object> goodsSerialNo,
        String recipientAddress,
        String supplierCode,
        String recipientPhoneNumber,
        String supplierName,
        String remark,
        String senderName,
        String senderPhoneNumber,
        String updatedByName,
        String updatedTime
) {
}
