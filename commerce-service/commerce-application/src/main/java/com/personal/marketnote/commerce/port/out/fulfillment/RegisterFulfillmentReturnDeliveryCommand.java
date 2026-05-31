package com.personal.marketnote.commerce.port.out.fulfillment;

import lombok.Builder;

import java.util.List;

@Builder
public record RegisterFulfillmentReturnDeliveryCommand(
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
        List<ProductItem> products
) {
    public record ProductItem(
            String productCode,
            Integer quantity
    ) {
        public static ProductItem of(String productCode, Integer quantity) {
            return new ProductItem(productCode, quantity);
        }
    }
}
