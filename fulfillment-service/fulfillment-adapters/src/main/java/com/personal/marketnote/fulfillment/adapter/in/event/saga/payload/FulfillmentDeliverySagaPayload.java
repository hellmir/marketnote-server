package com.personal.marketnote.fulfillment.adapter.in.event.saga.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderProductItem(
            Long pricePolicyId,
            UUID productKey,
            UUID sharerKey,
            Integer quantity,
            Long unitAmount
    ) {
    }
}
