package com.personal.marketnote.commerce.port.in.command.order;

import lombok.Builder;

import java.util.List;

@Builder
public record RegisterOrderCommand(
        Long buyerId,
        Long totalAmount,
        Long couponAmount,
        Long pointAmount,
        Long shippingFee,
        String recipientName,
        String recipientPhoneNumber,
        String zipCode,
        String address,
        String addressDetail,
        String requestMessage,
        List<OrderProductItemCommand> orderProducts
) {
}
