package com.personal.marketnote.commerce.adapter.out.persistence.ledger.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.ledger.entity.LedgerTransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerTransactionJpaRepository extends JpaRepository<LedgerTransactionJpaEntity, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);
}
