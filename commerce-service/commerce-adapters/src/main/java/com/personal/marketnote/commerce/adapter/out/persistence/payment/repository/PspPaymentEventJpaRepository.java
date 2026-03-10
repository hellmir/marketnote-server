package com.personal.marketnote.commerce.adapter.out.persistence.payment.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.payment.entity.PspPaymentEventJpaEntity;
import com.personal.marketnote.commerce.domain.payment.PaymentEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PspPaymentEventJpaRepository extends JpaRepository<PspPaymentEventJpaEntity, Long> {
    Optional<PspPaymentEventJpaEntity> findByOrderKey(String orderKey);

    List<PspPaymentEventJpaEntity> findAllByPoStatus(PaymentEventStatus poStatus);
}
