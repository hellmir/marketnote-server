package com.personal.marketnote.fulfillment.adapter.out.persistence.delivery;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.fulfillment.adapter.out.persistence.delivery.entity.FasstoDeliveryRegistrationJpaEntity;
import com.personal.marketnote.fulfillment.adapter.out.persistence.delivery.repository.FasstoDeliveryRegistrationJpaRepository;
import com.personal.marketnote.fulfillment.domain.delivery.FasstoDeliveryRegistration;
import com.personal.marketnote.fulfillment.exception.FasstoDeliveryAlreadyRegisteredException;
import com.personal.marketnote.fulfillment.port.out.delivery.SaveFasstoDeliveryRegistrationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

@PersistenceAdapter
@RequiredArgsConstructor
public class FasstoDeliveryRegistrationPersistenceAdapter implements SaveFasstoDeliveryRegistrationPort {
    private final FasstoDeliveryRegistrationJpaRepository repository;

    @Override
    public void save(FasstoDeliveryRegistration registration) {
        try {
            repository.saveAndFlush(FasstoDeliveryRegistrationJpaEntity.from(registration));
        } catch (DataIntegrityViolationException e) {
            throw new FasstoDeliveryAlreadyRegisteredException(registration.getOrderId());
        }
    }
}
