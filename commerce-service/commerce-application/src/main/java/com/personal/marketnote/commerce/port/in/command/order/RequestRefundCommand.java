package com.personal.marketnote.commerce.port.in.command.order;

import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import lombok.Builder;

@Builder
public record RequestRefundCommand(
        Long id,
        OrderStatusReasonCategory reasonCategory,
        String reason,
        Long buyerId,
        String pickupRecipientName,
        String pickupRecipientPhoneNumber,
        String pickupZipCode,
        String pickupAddress,
        String pickupAddressDetail,
        String pickupRequestMessage
) {
}
