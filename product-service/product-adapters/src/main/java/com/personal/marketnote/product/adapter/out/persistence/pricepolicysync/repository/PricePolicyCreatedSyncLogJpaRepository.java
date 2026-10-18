package com.personal.marketnote.product.adapter.out.persistence.pricepolicysync.repository;

import com.personal.marketnote.product.adapter.out.persistence.pricepolicysync.entity.PricePolicyCreatedSyncLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PricePolicyCreatedSyncLogJpaRepository extends JpaRepository<PricePolicyCreatedSyncLogJpaEntity, Long> {

    boolean existsByEventId(UUID eventId);
}
