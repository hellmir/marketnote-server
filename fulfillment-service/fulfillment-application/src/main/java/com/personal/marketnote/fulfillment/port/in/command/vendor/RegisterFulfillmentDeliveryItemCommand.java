package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

import java.util.List;

@Builder
public record RegisterFulfillmentDeliveryItemCommand(
        String orderDate,
        String orderNumber,
        Integer orderSequence,
        String slipNumber,
        String recipientName,
        String recipientPhoneNumber,
        String recipientAddress,
        String releaseMethod,
        String senderName,
        String senderPhoneNumber,
        String salesChannel,
        String shippingRequest,
        List<RegisterFulfillmentDeliveryGoodsCommand> products,
        String sameDayDeliveryCode,
        String remark
) {
}
