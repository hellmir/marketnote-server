package com.personal.marketnote.commerce.adapter.out.persistence.refund.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.refund.entity.RefundJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 환불 JPA 리포지토리.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
public interface RefundJpaRepository extends JpaRepository<RefundJpaEntity, Long> {

    List<RefundJpaEntity> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    List<RefundJpaEntity> findByPaymentIdOrderByCreatedAtDesc(Long paymentId);
}
