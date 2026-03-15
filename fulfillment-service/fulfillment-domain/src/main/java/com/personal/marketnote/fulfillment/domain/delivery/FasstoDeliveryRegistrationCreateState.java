package com.personal.marketnote.fulfillment.domain.delivery;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FasstoDeliveryRegistrationCreateState {
    private final Long orderId;
}
