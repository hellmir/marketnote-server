package com.personal.marketnote.fulfillment.adapter.out.persistence.delivery.repository;

import com.personal.marketnote.fulfillment.adapter.out.persistence.delivery.entity.FulfillmentDeliveryRegistrationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FulfillmentDeliveryRegistrationJpaRepository extends JpaRepository<FulfillmentDeliveryRegistrationJpaEntity, Long> {
    Optional<FulfillmentDeliveryRegistrationJpaEntity> findByOrderId(Long orderId);
}
