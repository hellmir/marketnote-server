package com.personal.marketnote.commerce.adapter.out.persistence.payment.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.payment.entity.PspPaymentEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PspPaymentEventJpaRepository extends JpaRepository<PspPaymentEventJpaEntity, Long> {
    Optional<PspPaymentEventJpaEntity> findByOrderKey(String orderKey);
}
