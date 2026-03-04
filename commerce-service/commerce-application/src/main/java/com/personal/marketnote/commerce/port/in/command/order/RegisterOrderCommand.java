package com.personal.marketnote.commerce.port.in.command.order;

import lombok.Builder;

import java.util.List;

@Builder
public record RegisterOrderCommand(
        Long buyerId,
        Long totalAmount,
        Long couponAmount,
        Long pointAmount,
        List<OrderProductItemCommand> orderProducts
) {
}
