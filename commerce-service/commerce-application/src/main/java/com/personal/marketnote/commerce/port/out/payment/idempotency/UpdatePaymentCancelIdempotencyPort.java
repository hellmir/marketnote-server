package com.personal.marketnote.commerce.port.out.payment.idempotency;

import com.personal.marketnote.commerce.domain.payment.idempotency.PaymentCancelIdempotency;

public interface UpdatePaymentCancelIdempotencyPort {
    void update(PaymentCancelIdempotency idempotency);
}
