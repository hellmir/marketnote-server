package com.personal.marketnote.commerce.port.in.command.order;

import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.Builder;

import java.util.List;

@Builder
public record RequestReturnCommand(
        Long id,
        OrderStatusReasonCategory reasonCategory,
        String reason,
        Long buyerId,
        List<Long> pricePolicyIds,
        String pickupRecipientName,
        String pickupRecipientPhoneNumber,
        String pickupZipCode,
        String pickupAddress,
        String pickupAddressDetail,
        String pickupRequestMessage
) {
    public boolean isPartialReturn() {
        return FormatValidator.hasValue(pricePolicyIds) && !pricePolicyIds.isEmpty();
    }
}
