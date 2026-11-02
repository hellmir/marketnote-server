package com.personal.marketnote.commerce.adapter.out.persistence.payment.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.payment.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, Long> {
    Optional<PaymentJpaEntity> findByOrderId(Long orderId);

    Optional<PaymentJpaEntity> findByOrderKey(UUID orderKey);
}
