package com.personal.marketnote.commerce.adapter.out.persistence.fulfillment.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.fulfillment.entity.FulfillmentWorkStatusReadModelJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FulfillmentWorkStatusReadModelJpaRepository extends JpaRepository<FulfillmentWorkStatusReadModelJpaEntity, Long> {
    Optional<FulfillmentWorkStatusReadModelJpaEntity> findByOrderId(Long orderId);
}
