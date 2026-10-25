package com.personal.marketnote.fulfillment.port.out.shipping;

import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FindShippingTrackerPort {
    List<ShippingTracker> findPollingActiveChunk(Long lastId, int limit);

    Optional<ShippingTracker> findByOrderId(Long orderId);

    Optional<LocalDateTime> findMinCreatedAtOfPollingActive();
}
