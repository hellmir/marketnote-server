package com.personal.marketnote.commerce.adapter.out.persistence.payment.idempotency;

import com.personal.marketnote.commerce.adapter.out.persistence.payment.idempotency.entity.PaymentCancelIdempotencyJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.payment.idempotency.mapper.PaymentCancelIdempotencyEntityToDomainMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.payment.idempotency.repository.PaymentCancelIdempotencyJpaRepository;
import com.personal.marketnote.commerce.domain.payment.idempotency.PaymentCancelIdempotency;
import com.personal.marketnote.commerce.exception.DuplicateIdempotencyKeyException;
import com.personal.marketnote.commerce.port.out.payment.idempotency.DeletePaymentCancelIdempotencyPort;
import com.personal.marketnote.commerce.port.out.payment.idempotency.FindPaymentCancelIdempotencyPort;
import com.personal.marketnote.commerce.port.out.payment.idempotency.SavePaymentCancelIdempotencyPort;
import com.personal.marketnote.commerce.port.out.payment.idempotency.UpdatePaymentCancelIdempotencyPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class PaymentCancelIdempotencyPersistenceAdapter
        implements SavePaymentCancelIdempotencyPort,
        FindPaymentCancelIdempotencyPort,
        UpdatePaymentCancelIdempotencyPort,
        DeletePaymentCancelIdempotencyPort {

    private final PaymentCancelIdempotencyJpaRepository repository;

    @Override
    public PaymentCancelIdempotency save(PaymentCancelIdempotency idempotency) {
        PaymentCancelIdempotencyJpaEntity entity = PaymentCancelIdempotencyJpaEntity.from(idempotency);
        try {
            PaymentCancelIdempotencyJpaEntity saved = repository.saveAndFlush(entity);
            return PaymentCancelIdempotencyEntityToDomainMapper.toDomain(saved);
        } catch (DataIntegrityViolationException dive) {
            throw new DuplicateIdempotencyKeyException(idempotency.getIdempotencyKey(), dive);
        }
    }

    @Override
    public Optional<PaymentCancelIdempotency> findByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey)
                .map(PaymentCancelIdempotencyEntityToDomainMapper::toDomain);
    }

    @Override
    public void update(PaymentCancelIdempotency idempotency) {
        PaymentCancelIdempotencyJpaEntity entity = repository.findByIdempotencyKey(idempotency.getIdempotencyKey())
                .orElseThrow(() -> new DomainNotFoundException(
                        "PaymentCancelIdempotency 엔티티를 찾을 수 없습니다. key=" + idempotency.getIdempotencyKey()
                ));
        entity.updateFrom(idempotency);
    }

    @Override
    public int deleteOlderThan(LocalDateTime threshold) {
        return repository.deleteByCreatedAtBefore(threshold);
    }
}
