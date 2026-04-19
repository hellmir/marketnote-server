package com.personal.marketnote.commerce.adapter.out.persistence.quickpayment.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.quickpayment.entity.QuickPaymentCardJpaEntity;
import com.personal.marketnote.common.domain.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuickPaymentCardJpaRepository extends JpaRepository<QuickPaymentCardJpaEntity, Long> {
    Optional<QuickPaymentCardJpaEntity> findByIdAndUserIdAndStatus(Long id, Long userId, EntityStatus status);
}
