package com.personal.marketnote.fulfillment.adapter.out.persistence.shipping.repository;

import com.personal.marketnote.fulfillment.adapter.out.persistence.shipping.entity.ShippingTrackerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShippingTrackerJpaRepository extends JpaRepository<ShippingTrackerJpaEntity, Long> {
    Optional<ShippingTrackerJpaEntity> findByOrderId(Long orderId);

    List<ShippingTrackerJpaEntity> findByPollingActiveTrue();
}
