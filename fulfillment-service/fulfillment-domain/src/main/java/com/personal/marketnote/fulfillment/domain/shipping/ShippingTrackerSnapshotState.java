package com.personal.marketnote.fulfillment.domain.shipping;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ShippingTrackerSnapshotState {
    private final Long id;
    private final Long orderId;
    private final String trackingNumber;
    private final String carrierCode;
    private final ShippingStatus shippingStatus;
    private final boolean pollingActive;
    private final LocalDateTime lastPolledAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
