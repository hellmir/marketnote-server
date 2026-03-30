package com.personal.marketnote.fulfillment.port.out.delivery;

import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentDeliveryRegistration;

public interface SaveFulfillmentDeliveryRegistrationPort {
    void save(FulfillmentDeliveryRegistration registration);
}
