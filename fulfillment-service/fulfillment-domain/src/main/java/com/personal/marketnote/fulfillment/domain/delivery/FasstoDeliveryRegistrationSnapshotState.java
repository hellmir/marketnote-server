package com.personal.marketnote.fulfillment.domain.delivery;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FasstoDeliveryRegistrationSnapshotState {
    private final Long id;
    private final Long orderId;
    private final LocalDateTime createdAt;
}
