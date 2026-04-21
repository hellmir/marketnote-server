package com.personal.marketnote.fulfillment.port.out.delivery;

import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentDeliveryRegistration;

import java.util.Optional;

public interface FindFulfillmentDeliveryRegistrationPort {
    Optional<FulfillmentDeliveryRegistration> findByOrderId(Long orderId);
}
