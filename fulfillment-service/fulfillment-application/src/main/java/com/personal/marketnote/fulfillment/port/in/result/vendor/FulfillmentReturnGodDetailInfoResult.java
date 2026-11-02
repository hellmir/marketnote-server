package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record FulfillmentReturnGodDetailInfoResult(
        String orderNumber,
        String supplierCode,
        String inboundSlipNumber,
        String customerCode,
        String warehouseCode,
        String inboundOrderSlipNumber,
        String inboundOrderDate,
        String releaseOrderSlipNumber,
        String supplierName,
        String recipientName,
        String releaseInvoiceNumber,
        String returnInvoiceNumber,
        String returnShippingPaymentCode,
        String returnShippingPaymentName,
        String returnShippingPayment,
        String returnMismatchYn,
        String returnType,
        String returnTypeName,
        String recipientPhoneNumber,
        String customerMemo,
        String returnReason,
        String returnReasonName,
        String returnDetailReason,
        String returnClassification,
        String returnClassificationName,
        List<FulfillmentReturnGodDetailGoodsResult> products
) {
    public static FulfillmentReturnGodDetailInfoResult of(
            String orderNumber,
            String supplierCode,
            String inboundSlipNumber,
            String customerCode,
            String warehouseCode,
            String inboundOrderSlipNumber,
            String inboundOrderDate,
            String releaseOrderSlipNumber,
            String supplierName,
            String recipientName,
            String releaseInvoiceNumber,
            String returnInvoiceNumber,
            String returnShippingPaymentCode,
            String returnShippingPaymentName,
            String returnShippingPayment,
            String returnMismatchYn,
            String returnType,
            String returnTypeName,
            String recipientPhoneNumber,
            String customerMemo,
            String returnReason,
            String returnReasonName,
            String returnDetailReason,
            String returnClassification,
            String returnClassificationName,
            List<FulfillmentReturnGodDetailGoodsResult> products
    ) {
        return new FulfillmentReturnGodDetailInfoResult(
                orderNumber, supplierCode, inboundSlipNumber, customerCode, warehouseCode, inboundOrderSlipNumber, inboundOrderDate,
                releaseOrderSlipNumber, supplierName, recipientName, releaseInvoiceNumber, returnInvoiceNumber,
                returnShippingPaymentCode, returnShippingPaymentName, returnShippingPayment, returnMismatchYn, returnType, returnTypeName,
                recipientPhoneNumber, customerMemo, returnReason, returnReasonName, returnDetailReason,
                returnClassification, returnClassificationName, products
        );
    }
}
