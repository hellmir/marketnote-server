package com.personal.marketnote.fulfillment.domain.delivery;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FulfillmentDeliveryRegistrationSnapshotState {
    private final Long id;
    private final Long orderId;
    private final FulfillmentWorkStatus workStatus;
    private final LocalDateTime createdAt;
}
