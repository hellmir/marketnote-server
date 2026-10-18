package com.personal.marketnote.fulfillment.adapter.in.event.saga.payload;

import java.util.List;
import java.util.UUID;

public record FulfillmentDeliverySagaPayload(
        Long orderId,
        Long buyerId,
        String recipientName,
        String recipientPhoneNumber,
        String zipCode,
        String address,
        String addressDetail,
        String deliveryRequestMessage,
        List<OrderProductItem> orderProducts
) {
    public record OrderProductItem(
            Long pricePolicyId,
            Long productId,
            UUID productKey,
            UUID sharerKey,
            Integer quantity,
            Long unitAmount
    ) {
    }
}
