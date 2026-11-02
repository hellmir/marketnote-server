package com.personal.marketnote.commerce.domain.order;

import com.personal.marketnote.commerce.domain.shipping.ShippingRegionType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderCreateState {
    private final Long buyerId;
    private final UUID sharerKey;
    private final OrderAmount amount;
    private final ShippingAddress shippingAddress;
    @Builder.Default
    private final ShippingRegionType shippingRegionType = ShippingRegionType.NORMAL;
    private final List<OrderProductCreateState> orderProductStates;
}

