package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

import java.util.List;

@Builder
public record RegisterFulfillmentDeliveryIcsItemCommand(
        String orderDate,
        String orderNumber,
        String platform,
        String logisticsCenter,
        String invoiceNumber,
        String recipientName,
        String recipientPhoneNumber,
        String recipientAddress,
        String senderName,
        String senderPhoneNumber,
        String salesChannel,
        String shippingRequest,
        String remark,
        List<RegisterFulfillmentDeliveryGoodsCommand> products
) {
}
