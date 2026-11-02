package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

import java.util.List;

@Builder
public record RegisterFulfillmentReturnDeliveryItemCommand(
        String orderDate,
        String orderNumber,
        String courierCode,
        String invoiceNumber,
        String recipientName,
        String recipientPhoneNumber,
        String recipientAddress,
        String returnReceiverName,
        String returnReceiverPhoneNumber,
        String returnZipCode,
        String returnAddress1,
        String returnAddress2,
        String returnType,
        String returnReason,
        String returnDetailReason,
        String returnShippingRequest,
        List<RegisterFulfillmentDeliveryGoodsCommand> products
) {
}
