package com.personal.marketnote.commerce.port.in.command.order;

import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import com.personal.marketnote.commerce.domain.order.ShippingAddress;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.Builder;

import java.util.List;

@Builder
public record ChangeOrderStatusCommand(
        Long id,
        List<Long> pricePolicyIds,
        OrderStatus orderStatus,
        OrderStatusReasonCategory reasonCategory,
        String reason,
        String role,
        Long buyerId,
        ShippingAddress pickupAddress
) {
    private static final String BUYER_ROLE = "BUYER";

    public boolean isPartialProductChange() {
        return FormatValidator.hasValue(pricePolicyIds);
    }

    /**
     * 구매자 역할인지 확인한다.
     *
     * @return role이 BUYER이면 true
     */
    public boolean isBuyerRole() {
        return BUYER_ROLE.equals(role);
    }

    /**
     * 서비스 내부 호출인지 확인한다.
     * role이 null이면 서비스 내부 호출로 간주한다.
     *
     * @return role이 null이면 true
     */
    public boolean isInternalCall() {
        return FormatValidator.hasNoValue(role);
    }
}
