package com.personal.marketnote.fulfillment.port.in.command;

import lombok.Builder;

import java.util.List;

@Builder
public record RegisterInternalReturnDeliveryCommand(
        Long orderId,
        String orderDate,
        String recipientName,
        String recipientPhoneNumber,
        String recipientAddress,
        String pickupRecipientName,
        String pickupRecipientPhoneNumber,
        String pickupZipCode,
        String pickupAddress,
        String pickupAddressDetail,
        String returnReason,
        String returnDetailReason,
        String returnShippingRequest,
        List<RegisterInternalReturnDeliveryProductCommand> products
) {
}
