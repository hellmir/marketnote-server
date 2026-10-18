package com.personal.marketnote.product.adapter.out.persistence.tagsync;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.product.adapter.out.persistence.tagsync.entity.ProductTagChangeSyncLogJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.tagsync.repository.ProductTagChangeSyncLogJpaRepository;
import com.personal.marketnote.product.port.out.tagsync.ProductTagChangeSyncLogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@PersistenceAdapter
@RequiredArgsConstructor
public class ProductTagChangeSyncLogPersistenceAdapter implements ProductTagChangeSyncLogPort {

    private final ProductTagChangeSyncLogJpaRepository repository;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public boolean isProcessed(UUID eventId) {
        return repository.existsByEventId(eventId);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void markProcessed(UUID eventId) {
        repository.saveAndFlush(ProductTagChangeSyncLogJpaEntity.of(eventId));
    }
}
