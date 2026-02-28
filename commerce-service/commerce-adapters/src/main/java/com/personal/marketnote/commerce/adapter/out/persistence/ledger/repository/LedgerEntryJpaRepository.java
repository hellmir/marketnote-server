package com.personal.marketnote.commerce.adapter.out.persistence.ledger.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.ledger.entity.LedgerEntryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryJpaEntity, Long> {
}
