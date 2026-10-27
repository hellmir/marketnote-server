package com.personal.marketnote.commerce.domain.payment.idempotency;

/**
 * 결제 취소 멱등성 처리 상태.
 *
 * <ul>
 *   <li>{@link #IN_PROGRESS}: 최초 요청 수신 후 비즈니스 처리 진행 중</li>
 *   <li>{@link #COMPLETED}: 비즈니스 TX 커밋 완료 (재요청 시 캐시 결과 반환)</li>
 *   <li>{@link #FAILED}: 처리 실패 (동일 키로 재시도 가능)</li>
 * </ul>
 */
public enum PaymentCancelIdempotencyStatus {
    IN_PROGRESS,
    COMPLETED,
    FAILED;

    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isFailed() {
        return this == FAILED;
    }
}
