package com.personal.marketnote.commerce.port.out.payment.idempotency;

import com.personal.marketnote.commerce.domain.payment.idempotency.PaymentCancelIdempotency;

import java.util.Optional;

public interface FindPaymentCancelIdempotencyPort {
    Optional<PaymentCancelIdempotency> findByIdempotencyKey(String idempotencyKey);
}
