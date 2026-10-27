package com.personal.marketnote.commerce.adapter.out.persistence.payment.idempotency.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.payment.idempotency.entity.PaymentCancelIdempotencyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentCancelIdempotencyJpaRepository extends JpaRepository<PaymentCancelIdempotencyJpaEntity, Long> {

    Optional<PaymentCancelIdempotencyJpaEntity> findByIdempotencyKey(String idempotencyKey);

    @Modifying
    @Query("DELETE FROM PaymentCancelIdempotencyJpaEntity e WHERE e.createdAt < :threshold")
    int deleteByCreatedAtBefore(@Param("threshold") LocalDateTime threshold);
}
