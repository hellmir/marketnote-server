package com.personal.marketnote.commerce.port.in.command.order;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import lombok.Builder;

import java.util.List;

@Builder
public record RegisterOrderCommand(
        Long buyerId,
        OrderAmountCommand amount,
        Long shippingAddressId,
        DeliveryRequestType deliveryRequestType,
        String deliveryRequestMessage,
        List<OrderProductItemCommand> orderProducts
) {
}
