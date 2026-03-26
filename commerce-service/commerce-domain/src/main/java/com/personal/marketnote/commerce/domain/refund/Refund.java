package com.personal.marketnote.commerce.domain.refund;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 환불 도메인 모델.
 * <p>
 * 결제 취소 시 생성되는 환불 상세 정보를 관리한다.
 * 환불 유형(전체/부분), 환불 금액, 취소 사유, 처리자, PG 응답 정보를 포함한다.
 * </p>
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Refund {
    private Long id;
    private Long paymentId;
    private Long orderId;
    private RefundType refundType;
    private Long refundAmount;
    private String cancelReason;
    private String processedBy;
    private String pgRefundKey;
    private String pgRawResponse;
    private LocalDateTime createdAt;

    /**
     * 새 환불을 생성한다.
     *
     * @param state 환불 생성 상태 객체
     * @return 새 환불 도메인 객체
     * @throws RefundPaymentIdNoValueException 결제 ID가 null인 경우
     * @throws RefundOrderIdNoValueException   주문 ID가 null인 경우
     * @throws RefundTypeNoValueException      환불 유형이 null인 경우
     * @throws InvalidRefundAmountException    환불 금액이 0 이하인 경우
     */
    public static Refund from(RefundCreateState state) {
        validate(state);
        return Refund.builder()
                .paymentId(state.getPaymentId())
                .orderId(state.getOrderId())
                .refundType(state.getRefundType())
                .refundAmount(state.getRefundAmount())
                .cancelReason(state.getCancelReason())
                .processedBy(state.getProcessedBy())
                .pgRefundKey(state.getPgRefundKey())
                .pgRawResponse(state.getPgRawResponse())
                .build();
    }

    /**
     * 영속화된 상태로부터 환불 도메인 객체를 복원한다.
     *
     * @param state 환불 스냅샷 상태 객체
     * @return 복원된 환불 도메인 객체
     */
    public static Refund from(RefundSnapshotState state) {
        return Refund.builder()
                .id(state.getId())
                .paymentId(state.getPaymentId())
                .orderId(state.getOrderId())
                .refundType(state.getRefundType())
                .refundAmount(state.getRefundAmount())
                .cancelReason(state.getCancelReason())
                .processedBy(state.getProcessedBy())
                .pgRefundKey(state.getPgRefundKey())
                .pgRawResponse(state.getPgRawResponse())
                .createdAt(state.getCreatedAt())
                .build();
    }

    /**
     * 전체 환불인지 확인한다.
     *
     * @return 전체 환불이면 true
     */
    public boolean isFullRefund() {
        return this.refundType == RefundType.FULL_REFUND;
    }

    /**
     * 부분 환불인지 확인한다.
     *
     * @return 부분 환불이면 true
     */
    public boolean isPartialRefund() {
        return this.refundType == RefundType.PARTIAL_REFUND;
    }

    private static void validate(RefundCreateState state) {
        if (FormatValidator.hasNoValue(state.getPaymentId())) {
            throw new RefundPaymentIdNoValueException();
        }
        if (FormatValidator.hasNoValue(state.getOrderId())) {
            throw new RefundOrderIdNoValueException();
        }
        if (FormatValidator.hasNoValue(state.getRefundType())) {
            throw new RefundTypeNoValueException();
        }
        if (FormatValidator.hasNoValue(state.getRefundAmount()) || state.getRefundAmount() <= 0) {
            throw new InvalidRefundAmountException();
        }
    }
}
