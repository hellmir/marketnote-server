package com.personal.marketnote.commerce.port.in.command.payment;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.Builder;

import java.util.List;

@Builder
public record CancelPaymentCommand(
        Long buyerId,
        String orderKey,
        CancelType cancelType,
        Long cancelAmount,
        String cancelReason,
        List<CancelProductItem> cancelProducts
) {
    public enum CancelType {
        FULL, PARTIAL
    }

    public record CancelProductItem(
            Long pricePolicyId,
            Integer quantity
    ) {
    }

    public boolean isFullCancel() {
        return cancelType == CancelType.FULL;
    }

    public boolean hasCancelProducts() {
        return FormatValidator.hasValue(cancelProducts) && !cancelProducts.isEmpty();
    }
}
