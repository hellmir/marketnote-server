package com.personal.marketnote.user.port.in.command.shippingaddress;

import com.personal.marketnote.user.domain.shippingaddress.DeliveryRequestType;
import lombok.Builder;

@Builder
public record UpdateDeliveryRequestCommand(
        DeliveryRequestType deliveryRequestType,
        String deliveryRequestMessage
) {
}
