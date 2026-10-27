package com.personal.marketnote.commerce.domain.payment.idempotency;

public class InvalidPaymentCancelIdempotencyStatusTransitionException extends IllegalStateException {
    public InvalidPaymentCancelIdempotencyStatusTransitionException(PaymentCancelIdempotencyStatus current) {
        super("ERR_PAYMENT_CANCEL_IDEMPOTENCY_01::결제 취소 멱등성 상태 전이가 허용되지 않습니다. 현재 상태: " + current);
    }
}
