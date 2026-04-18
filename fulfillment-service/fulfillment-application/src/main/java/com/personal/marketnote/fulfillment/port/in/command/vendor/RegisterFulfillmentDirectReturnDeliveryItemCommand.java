package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

import java.util.List;

@Builder
public record RegisterFulfillmentDirectReturnDeliveryItemCommand(
        String orderDate,
        String supplierCode,
        String originalCourierCode,
        String originalInvoiceNumber,
        String returnReceiveMethod,
        String recipientName,
        String returnCourierCompany,
        String returnInvoiceNumber,
        String returnType,
        String returnReason,
        String returnDetailReason,
        String remark,
        List<RegisterFulfillmentDeliveryGoodsCommand> products
) {
}
