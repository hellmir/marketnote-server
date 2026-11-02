package com.personal.marketnote.user.port.in.command.shippingaddress;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import lombok.Builder;

@Builder
public record RegisterShippingAddressCommand(
        Long userId,
        ShippingAddressType addressType,
        String address,
        String addressDetail,
        String companyName,
        String addressAlias,
        String recipientName,
        String recipientPhoneNumber,
        DeliveryRequestType deliveryRequestType,
        String deliveryRequestMessage,
        Boolean isDefault
) {
}
