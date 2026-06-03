package com.personal.marketnote.commerce.domain.settlement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentAllocationTransactionType {
    ORDER_REGISTRATION("주문 등록 배분"),
    CANCELLATION("취소 역배분"),
    PARTIAL_REFUND("부분 환불 역배분"),
    RETURN_REFUND("반품 환불 역배분");

    private final String description;

    public boolean isPositiveContribution() {
        return this == ORDER_REGISTRATION;
    }

    public boolean isNegativeContribution() {
        return this == CANCELLATION || this == PARTIAL_REFUND || this == RETURN_REFUND;
    }
}
