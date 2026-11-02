package com.personal.marketnote.fulfillment.adapter.out.persistence.shipping;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.adapter.out.persistence.shipping.entity.ShippingTrackerJpaEntity;
import com.personal.marketnote.fulfillment.adapter.out.persistence.shipping.repository.ShippingTrackerJpaRepository;
import com.personal.marketnote.fulfillment.domain.exception.ShippingTrackerNotFoundException;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;
import com.personal.marketnote.fulfillment.exception.ShippingTrackerAlreadyExistsException;
import com.personal.marketnote.fulfillment.port.out.shipping.FindShippingTrackerPort;
import com.personal.marketnote.fulfillment.port.out.shipping.SaveShippingTrackerPort;
import com.personal.marketnote.fulfillment.port.out.shipping.UpdateShippingTrackerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class ShippingTrackerPersistenceAdapter implements
        SaveShippingTrackerPort,
        FindShippingTrackerPort,
        UpdateShippingTrackerPort {

    private final ShippingTrackerJpaRepository repository;

    @Override
    public void save(ShippingTracker shippingTracker) {
        try {
            repository.saveAndFlush(ShippingTrackerJpaEntity.from(shippingTracker));
        } catch (DataIntegrityViolationException e) {
            throw new ShippingTrackerAlreadyExistsException(shippingTracker.getOrderId());
        }
    }

    @Override
    public List<ShippingTracker> findPollingActiveChunk(Long lastId, int limit) {
        return repository.findPollingActiveChunk(lastId, PageRequest.of(0, limit)).stream()
                .map(ShippingTrackerJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<ShippingTracker> findByOrderId(Long orderId) {
        return repository.findByOrderId(orderId)
                .map(ShippingTrackerJpaEntity::toDomain);
    }

    @Override
    public Optional<LocalDateTime> findMinCreatedAtOfPollingActive() {
        return repository.findMinCreatedAtOfPollingActive();
    }

    @Override
    public void update(ShippingTracker shippingTracker) {
        if (FormatValidator.hasNoValue(shippingTracker.getId())) {
            throw new ShippingTrackerNotFoundException(shippingTracker.getOrderId());
        }
        repository.saveAndFlush(ShippingTrackerJpaEntity.from(shippingTracker));
    }
}
