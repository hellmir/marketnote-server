package com.personal.marketnote.commerce.adapter.out.persistence.settlement.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.settlement.entity.PaymentAllocationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAllocationJpaRepository extends JpaRepository<PaymentAllocationJpaEntity, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);
}
