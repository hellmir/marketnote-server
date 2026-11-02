package com.personal.marketnote.fulfillment.adapter.out.persistence.shipping.repository;

import com.personal.marketnote.fulfillment.adapter.out.persistence.shipping.entity.ShippingTrackerJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShippingTrackerJpaRepository extends JpaRepository<ShippingTrackerJpaEntity, Long> {
    Optional<ShippingTrackerJpaEntity> findByOrderId(Long orderId);

    @Query("""
            SELECT s FROM ShippingTrackerJpaEntity s
            WHERE s.pollingActive = true
              AND s.id > :lastId
            ORDER BY s.id ASC
            """)
    List<ShippingTrackerJpaEntity> findPollingActiveChunk(@Param("lastId") Long lastId, Pageable pageable);

    @Query("""
            SELECT MIN(s.createdAt) FROM ShippingTrackerJpaEntity s
            WHERE s.pollingActive = true
            """)
    Optional<LocalDateTime> findMinCreatedAtOfPollingActive();
}
