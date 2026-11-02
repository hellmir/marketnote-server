package com.personal.marketnote.commerce.domain.settlement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentAllocationTargetType {
    ORDER("주문"),
    REFUND("환불");

    private final String description;
}
