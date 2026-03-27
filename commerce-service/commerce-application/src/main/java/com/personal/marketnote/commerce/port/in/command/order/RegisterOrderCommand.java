package com.personal.marketnote.commerce.port.in.command.order;

import lombok.Builder;

import java.util.List;

@Builder
public record RegisterOrderCommand(
        Long buyerId,
        OrderAmountCommand amount,
        Long shippingAddressId,
        String requestMessage,
        List<OrderProductItemCommand> orderProducts
) {
}
