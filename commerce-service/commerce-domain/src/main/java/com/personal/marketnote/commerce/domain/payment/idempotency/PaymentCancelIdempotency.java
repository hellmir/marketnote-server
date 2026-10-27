package com.personal.marketnote.commerce.domain.payment.idempotency;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 결제 취소 API 멱등성 레코드.
 *
 * <p>Idempotency-Key 헤더 단위로 처리 상태/결과를 저장하여 중복 요청을 방지한다.
 *
 * <p>상태 전이:
 * <pre>
 *   (신규) → IN_PROGRESS
 *   IN_PROGRESS → COMPLETED (complete)
 *   IN_PROGRESS → FAILED    (fail)
 *   FAILED      → IN_PROGRESS (retry) — 동일 키로 재시도 허용
 * </pre>
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class PaymentCancelIdempotency {
    private Long id;
    private String idempotencyKey;
    private Long paymentId;
    private Long buyerId;
    private PaymentCancelIdempotencyStatus status;
    private String result;
    private String failedReason;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static PaymentCancelIdempotency from(PaymentCancelIdempotencyCreateState state) {
        return PaymentCancelIdempotency.builder()
                .idempotencyKey(state.getIdempotencyKey())
                .paymentId(state.getPaymentId())
                .buyerId(state.getBuyerId())
                .status(PaymentCancelIdempotencyStatus.IN_PROGRESS)
                .build();
    }

    public static PaymentCancelIdempotency from(PaymentCancelIdempotencySnapshotState state) {
        return PaymentCancelIdempotency.builder()
                .id(state.getId())
                .idempotencyKey(state.getIdempotencyKey())
                .paymentId(state.getPaymentId())
                .buyerId(state.getBuyerId())
                .status(state.getStatus())
                .result(state.getResult())
                .failedReason(state.getFailedReason())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }

    public boolean isInProgress() {
        return status.isInProgress();
    }

    public boolean isCompleted() {
        return status.isCompleted();
    }

    public boolean isFailed() {
        return status.isFailed();
    }

    public boolean canRetry() {
        return status.isFailed();
    }

    public void complete(String resultJson) {
        if (!isInProgress()) {
            throw new InvalidPaymentCancelIdempotencyStatusTransitionException(status);
        }
        status = PaymentCancelIdempotencyStatus.COMPLETED;
        result = resultJson;
        failedReason = null;
    }

    public void fail(String reason) {
        if (!isInProgress()) {
            throw new InvalidPaymentCancelIdempotencyStatusTransitionException(status);
        }
        status = PaymentCancelIdempotencyStatus.FAILED;
        failedReason = FormatValidator.hasValue(reason) ? reason : "unknown";
    }

    public void retry() {
        if (!canRetry()) {
            throw new InvalidPaymentCancelIdempotencyStatusTransitionException(status);
        }
        status = PaymentCancelIdempotencyStatus.IN_PROGRESS;
        failedReason = null;
    }
}
