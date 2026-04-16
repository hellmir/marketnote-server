package com.personal.marketnote.commerce.adapter.out.persistence.quickpayment.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.quickpayment.entity.QuickPaymentCardJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuickPaymentCardJpaRepository extends JpaRepository<QuickPaymentCardJpaEntity, Long> {
}
