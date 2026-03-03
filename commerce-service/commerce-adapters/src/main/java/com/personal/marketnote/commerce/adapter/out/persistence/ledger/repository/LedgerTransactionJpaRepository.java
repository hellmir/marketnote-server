package com.personal.marketnote.commerce.adapter.out.persistence.ledger.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.ledger.entity.LedgerTransactionJpaEntity;
import com.personal.marketnote.commerce.domain.ledger.LedgerTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LedgerTransactionJpaRepository extends JpaRepository<LedgerTransactionJpaEntity, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("""
            SELECT t FROM LedgerTransactionJpaEntity t
            WHERE (:startDate IS NULL OR t.createdAt >= :startDate)
            AND (:endDate IS NULL OR t.createdAt <= :endDate)
            AND (:transactionType IS NULL OR t.transactionType = :transactionType)
            ORDER BY t.createdAt DESC
            """)
    List<LedgerTransactionJpaEntity> findByFilters(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("transactionType") LedgerTransactionType transactionType
    );
}
