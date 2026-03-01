package com.personal.marketnote.commerce.adapter.out.persistence.settlement.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.settlement.entity.PaymentAllocationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentAllocationJpaRepository extends JpaRepository<PaymentAllocationJpaEntity, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("SELECT pa FROM PaymentAllocationJpaEntity pa WHERE pa.settlementId IS NULL " +
            "AND YEAR(pa.createdAt) = :year AND MONTH(pa.createdAt) = :month")
    List<PaymentAllocationJpaEntity> findAllUnsettledByPeriod(@Param("year") Integer year, @Param("month") Integer month);

    @Modifying
    @Query("UPDATE PaymentAllocationJpaEntity pa SET pa.settlementId = :settlementId WHERE pa.id IN :ids AND pa.settlementId IS NULL")
    int bulkAssignSettlement(@Param("ids") List<Long> ids, @Param("settlementId") Long settlementId);

    List<PaymentAllocationJpaEntity> findAllBySettlementId(Long settlementId);
}
