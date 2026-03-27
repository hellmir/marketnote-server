package com.personal.marketnote.commerce.domain.order;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderAmountCreateState {
    private final Long totalAmount;
    private final Long couponAmount;
    private final Long pointAmount;
    private final Long shippingFee;
}
