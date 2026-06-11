package com.personal.marketnote.fulfillment.port.out.shipping;

import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;

import java.util.List;
import java.util.Optional;

public interface FindShippingTrackerPort {
    List<ShippingTracker> findAllPollingActive();

    Optional<ShippingTracker> findByOrderId(Long orderId);
}
