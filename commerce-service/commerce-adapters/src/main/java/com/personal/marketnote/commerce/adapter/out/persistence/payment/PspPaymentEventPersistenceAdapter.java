package com.personal.marketnote.commerce.adapter.out.persistence.payment;

import com.personal.marketnote.commerce.adapter.out.persistence.payment.entity.PspPaymentEventJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.payment.mapper.PspPaymentEventEntityToDomainMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.payment.repository.PspPaymentEventJpaRepository;
import com.personal.marketnote.commerce.domain.payment.PaymentEventStatus;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.port.out.payment.FindPspPaymentEventPort;
import com.personal.marketnote.commerce.port.out.payment.SavePspPaymentEventPort;
import com.personal.marketnote.commerce.port.out.payment.UpdatePspPaymentEventPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class PspPaymentEventPersistenceAdapter implements SavePspPaymentEventPort, FindPspPaymentEventPort, UpdatePspPaymentEventPort {
    private final PspPaymentEventJpaRepository pspPaymentEventJpaRepository;

    @Override
    public PspPaymentEvent save(PspPaymentEvent event) {
        PspPaymentEventJpaEntity entity = PspPaymentEventJpaEntity.from(event);
        PspPaymentEventJpaEntity savedEntity = pspPaymentEventJpaRepository.save(entity);
        return PspPaymentEventEntityToDomainMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PspPaymentEvent> findByOrderKey(String orderKey) {
        return pspPaymentEventJpaRepository.findByOrderKey(orderKey)
                .map(PspPaymentEventEntityToDomainMapper::toDomain);
    }

    @Override
    public List<PspPaymentEvent> findAllByUnknownStatus() {
        return pspPaymentEventJpaRepository.findAllByPoStatus(PaymentEventStatus.UNKNOWN).stream()
                .map(PspPaymentEventEntityToDomainMapper::toDomain)
                .toList();
    }

    @Override
    public void update(PspPaymentEvent event) {
        PspPaymentEventJpaEntity entity = pspPaymentEventJpaRepository.findByOrderKey(event.getOrderKey())
                .orElseThrow(() -> new DomainNotFoundException("PspPaymentEvent 엔티티를 찾을 수 없습니다."));
        entity.updateFrom(event);
    }
}
