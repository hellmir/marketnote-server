package com.personal.marketnote.fulfillment.port.out.delivery;

import com.personal.marketnote.fulfillment.domain.delivery.FasstoDeliveryRegistration;

public interface SaveFasstoDeliveryRegistrationPort {
    void save(FasstoDeliveryRegistration registration);
}
