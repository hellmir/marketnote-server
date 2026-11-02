package com.personal.marketnote.product.adapter.out.persistence.pricepolicysync;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicysync.entity.PricePolicyCreatedSyncLogJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicysync.repository.PricePolicyCreatedSyncLogJpaRepository;
import com.personal.marketnote.product.port.out.pricepolicysync.PricePolicyCreatedSyncLogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@PersistenceAdapter
@RequiredArgsConstructor
public class PricePolicyCreatedSyncLogPersistenceAdapter implements PricePolicyCreatedSyncLogPort {

    private final PricePolicyCreatedSyncLogJpaRepository repository;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public boolean isProcessed(UUID eventId) {
        return repository.existsByEventId(eventId);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void markProcessed(UUID eventId) {
        repository.saveAndFlush(PricePolicyCreatedSyncLogJpaEntity.of(eventId));
    }
}
