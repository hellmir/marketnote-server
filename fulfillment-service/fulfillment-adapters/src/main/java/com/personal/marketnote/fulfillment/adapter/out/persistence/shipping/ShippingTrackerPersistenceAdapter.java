package com.personal.marketnote.fulfillment.adapter.out.persistence.shipping;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.fulfillment.adapter.out.persistence.shipping.entity.ShippingTrackerJpaEntity;
import com.personal.marketnote.fulfillment.adapter.out.persistence.shipping.repository.ShippingTrackerJpaRepository;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;
import com.personal.marketnote.fulfillment.exception.ShippingTrackerAlreadyExistsException;
import com.personal.marketnote.fulfillment.port.out.shipping.SaveShippingTrackerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

@PersistenceAdapter
@RequiredArgsConstructor
public class ShippingTrackerPersistenceAdapter implements SaveShippingTrackerPort {
    private final ShippingTrackerJpaRepository repository;

    @Override
    public void save(ShippingTracker shippingTracker) {
        try {
            repository.saveAndFlush(ShippingTrackerJpaEntity.from(shippingTracker));
        } catch (DataIntegrityViolationException e) {
            throw new ShippingTrackerAlreadyExistsException(shippingTracker.getOrderId());
        }
    }
}
