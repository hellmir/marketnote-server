package com.personal.marketnote.fulfillment.port.out.shipping;

import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;

public interface SaveShippingTrackerPort {
    void save(ShippingTracker shippingTracker);
}
