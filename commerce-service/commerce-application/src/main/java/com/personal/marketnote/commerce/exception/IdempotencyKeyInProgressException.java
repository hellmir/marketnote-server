package com.personal.marketnote.commerce.exception;

/**
 * 동일 Idempotency-Key 로 이미 처리 중인 요청이 존재하는 경우 던져지는 예외.
 *
 * <p>409 Conflict + {@code {"status":"IN_PROGRESS"}} 로 응답한다.
 */
public class IdempotencyKeyInProgressException extends RuntimeException {
    public IdempotencyKeyInProgressException(String idempotencyKey) {
        super("ERR_PAYMENT_CANCEL_IDEMPOTENCY_11::동일 멱등성 키로 이미 처리 중입니다. idempotencyKey=" + idempotencyKey);
    }
}
