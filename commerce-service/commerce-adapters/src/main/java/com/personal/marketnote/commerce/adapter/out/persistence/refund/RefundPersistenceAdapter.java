package com.personal.marketnote.commerce.adapter.out.persistence.refund;

import com.personal.marketnote.commerce.adapter.out.persistence.refund.entity.RefundJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.refund.mapper.RefundJpaEntityToDomainMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.refund.repository.RefundJpaRepository;
import com.personal.marketnote.commerce.domain.refund.Refund;
import com.personal.marketnote.commerce.port.out.refund.FindRefundPort;
import com.personal.marketnote.commerce.port.out.refund.SaveRefundPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 환불 퍼시스턴스 어댑터.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@PersistenceAdapter
@RequiredArgsConstructor
public class RefundPersistenceAdapter implements SaveRefundPort, FindRefundPort {
    private final RefundJpaRepository refundJpaRepository;

    @Override
    public Refund save(Refund refund) {
        RefundJpaEntity entity = RefundJpaEntity.from(refund);
        RefundJpaEntity savedEntity = refundJpaRepository.save(entity);
        return RefundJpaEntityToDomainMapper.toDomain(savedEntity);
    }

    @Override
    public List<Refund> findByOrderId(Long orderId) {
        return refundJpaRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(RefundJpaEntityToDomainMapper::toDomain)
                .toList();
    }

    @Override
    public List<Refund> findByPaymentId(Long paymentId) {
        return refundJpaRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId).stream()
                .map(RefundJpaEntityToDomainMapper::toDomain)
                .toList();
    }
}
