package com.personal.marketnote.user.port.in.command.shippingaddress;

import com.personal.marketnote.user.domain.shippingaddress.DeliveryRequestType;
import lombok.Builder;

@Builder
public record UpdateShippingAddressCommand(
        String address,
        String addressDetail,
        String companyName,
        String addressAlias,
        String recipientName,
        String recipientPhoneNumber,
        DeliveryRequestType deliveryRequestType,
        String deliveryRequestMessage
) {
}
