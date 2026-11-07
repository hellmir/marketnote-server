package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

import lombok.Builder;

@Builder
public record FulfillmentDeliveryStatusInfoResult(
        String boxDivision,
        String boxDivisionName,
        String boxName,
        String boxNumber,
        String boxType,
        String cargoStatus,
        String cargoStatusName,
        String customerCode,
        String customerName,
        String recipientAddress,
        String recipientName,
        String recipientPhoneNumber,
        String deliveryMismatchYn,
        String productCode,
        String productName,
        String invoiceNumber,
        String orderNumber,
        String orderSequence,
        String releaseDivision,
        String releaseDivisionName,
        String releaseOrderSlipNumber,
        String packingDate,
        String packingQuantity,
        String packingSequence,
        String courierCode,
        String courierLinkYn,
        String courierName,
        String pickingSequence,
        String postYn,
        String printCount,
        String returnAddress1,
        String returnAddress2,
        String returnCheck,
        String returnEmployeeName,
        String returnOrderDate,
        String returnPhoneNumber,
        String returnZipCode,
        String salesChannel,
        String shippingRequestTerm,
        String shopCode,
        String shopName,
        String sku,
        String warehouseCode,
        List<Object> goodsSerialNo,
        String supplierCode,
        String supplierName
) {
}
