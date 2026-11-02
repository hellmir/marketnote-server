package com.personal.marketnote.commerce.exception;

/**
 * 결제 취소 Idempotency-Key UNIQUE 제약 충돌 시 던져지는 예외.
 *
 * <p>동시 요청 race 에서 두 번째 INSERT 가 실패하면 Helper 가 재조회로 흐름을 흡수한다.
 */
public class DuplicateIdempotencyKeyException extends RuntimeException {
    public DuplicateIdempotencyKeyException(String idempotencyKey) {
        super("ERR_PAYMENT_CANCEL_IDEMPOTENCY_10::이미 사용된 멱등성 키입니다. idempotencyKey=" + idempotencyKey);
    }

    public DuplicateIdempotencyKeyException(String idempotencyKey, Throwable cause) {
        super("ERR_PAYMENT_CANCEL_IDEMPOTENCY_10::이미 사용된 멱등성 키입니다. idempotencyKey=" + idempotencyKey, cause);
    }
}
