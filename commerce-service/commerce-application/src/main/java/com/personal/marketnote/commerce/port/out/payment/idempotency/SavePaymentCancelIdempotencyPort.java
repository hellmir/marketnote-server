package com.personal.marketnote.commerce.port.out.payment.idempotency;

import com.personal.marketnote.commerce.domain.payment.idempotency.PaymentCancelIdempotency;

/**
 * 결제 취소 멱등성 레코드 저장 포트.
 *
 * <p>UNIQUE(idempotency_key) 충돌 시 {@code DuplicateIdempotencyKeyException} 을 던진다.
 */
public interface SavePaymentCancelIdempotencyPort {
    PaymentCancelIdempotency save(PaymentCancelIdempotency idempotency);
}
