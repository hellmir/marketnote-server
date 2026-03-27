package com.personal.marketnote.commerce.port.in.command.order;

import com.personal.marketnote.commerce.domain.order.ShippingAddress;
import lombok.Builder;

import java.util.List;

@Builder
public record RegisterOrderCommand(
        Long buyerId,
        OrderAmountCommand amount,
        ShippingAddress shippingAddress,
        List<OrderProductItemCommand> orderProducts
) {
}
