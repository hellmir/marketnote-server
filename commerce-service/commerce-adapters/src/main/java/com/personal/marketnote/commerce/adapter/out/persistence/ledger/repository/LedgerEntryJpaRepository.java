package com.personal.marketnote.commerce.adapter.out.persistence.ledger.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.ledger.entity.LedgerEntryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryJpaEntity, Long> {
    List<LedgerEntryJpaEntity> findByTransactionIdOrderByIdAsc(Long transactionId);

    @Query("""
            SELECT e.accountId, e.transactionType, SUM(e.amount)
            FROM LedgerEntryJpaEntity e
            WHERE e.createdAt <= :asOf
            GROUP BY e.accountId, e.transactionType
            ORDER BY e.accountId ASC
            """)
    List<Object[]> findAccountBalanceSummary(@Param("asOf") LocalDateTime asOf);
}
