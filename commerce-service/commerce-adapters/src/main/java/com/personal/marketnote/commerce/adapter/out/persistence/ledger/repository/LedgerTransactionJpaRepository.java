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
            WHERE t.createdAt >= :startDate
            AND t.createdAt <= :endDate
            ORDER BY t.createdAt DESC
            """)
    List<LedgerTransactionJpaEntity> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
            SELECT t FROM LedgerTransactionJpaEntity t
            WHERE t.createdAt >= :startDate
            AND t.createdAt <= :endDate
            AND t.transactionType = :transactionType
            ORDER BY t.createdAt DESC
            """)
    List<LedgerTransactionJpaEntity> findByDateRangeAndType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("transactionType") LedgerTransactionType transactionType
    );
}
