package com.personal.marketnote.fulfillment.adapter.out.persistence.delivery;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.fulfillment.adapter.out.persistence.delivery.entity.FulfillmentDeliveryRegistrationJpaEntity;
import com.personal.marketnote.fulfillment.adapter.out.persistence.delivery.repository.FulfillmentDeliveryRegistrationJpaRepository;
import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentDeliveryRegistration;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryAlreadyRegisteredException;
import com.personal.marketnote.fulfillment.port.out.delivery.SaveFulfillmentDeliveryRegistrationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

@PersistenceAdapter
@RequiredArgsConstructor
public class FulfillmentDeliveryRegistrationPersistenceAdapter implements SaveFulfillmentDeliveryRegistrationPort {
    private final FulfillmentDeliveryRegistrationJpaRepository repository;

    @Override
    public void save(FulfillmentDeliveryRegistration registration) {
        try {
            repository.saveAndFlush(FulfillmentDeliveryRegistrationJpaEntity.from(registration));
        } catch (DataIntegrityViolationException e) {
            throw new FulfillmentDeliveryAlreadyRegisteredException(registration.getOrderId());
        }
    }
}
