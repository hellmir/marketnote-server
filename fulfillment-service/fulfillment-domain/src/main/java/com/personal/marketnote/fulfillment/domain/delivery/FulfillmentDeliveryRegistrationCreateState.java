package com.personal.marketnote.fulfillment.domain.delivery;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FulfillmentDeliveryRegistrationCreateState {
    private final Long orderId;
}
