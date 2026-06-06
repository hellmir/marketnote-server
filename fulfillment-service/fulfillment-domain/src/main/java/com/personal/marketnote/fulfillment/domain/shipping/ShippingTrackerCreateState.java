package com.personal.marketnote.fulfillment.domain.shipping;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShippingTrackerCreateState {
    private final Long orderId;
}
