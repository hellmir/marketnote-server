package com.personal.marketnote.commerce.adapter.out.persistence.payment;

import com.personal.marketnote.commerce.adapter.out.persistence.payment.entity.PaymentJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.payment.mapper.PaymentEntityToDomainMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.payment.repository.PaymentJpaRepository;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.port.out.payment.FindPaymentPort;
import com.personal.marketnote.commerce.port.out.payment.SavePaymentPort;
import com.personal.marketnote.commerce.port.out.payment.UpdatePaymentPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@PersistenceAdapter
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements SavePaymentPort, FindPaymentPort, UpdatePaymentPort {
    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = PaymentJpaEntity.from(payment);
        PaymentJpaEntity savedEntity = paymentJpaRepository.save(entity);
        return PaymentEntityToDomainMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        return paymentJpaRepository.findByOrderId(orderId)
                .map(PaymentEntityToDomainMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByOrderKey(UUID orderKey) {
        return paymentJpaRepository.findByOrderKey(orderKey)
                .map(PaymentEntityToDomainMapper::toDomain);
    }

    @Override
    public void update(Payment payment) {
        PaymentJpaEntity entity = paymentJpaRepository.findByOrderKey(payment.getOrderKey())
                .orElseThrow(() -> new EntityNotFoundException("결제 엔티티를 찾을 수 없습니다."));
        entity.updateFrom(payment);
    }
}
