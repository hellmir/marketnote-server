package com.personal.marketnote.commerce.domain.order;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderCreateState {
    private final Long buyerId;
    private final Long sharerId;
    private final Long totalAmount;
    private final Long couponAmount;
    private final Long pointAmount;
    private final List<OrderProductCreateState> orderProductStates;
}

