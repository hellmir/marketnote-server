package com.personal.marketnote.common.kafka.event;

public record ShippingAddressChangedEvent(
        Long shippingAddressId,
        Long userId,
        String recipientName,
        String recipientPhoneNumber,
        String address,
        ShippingAddressChangeAction action
) {
}
