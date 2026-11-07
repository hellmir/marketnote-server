package com.personal.marketnote.commerce.domain.refund;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 환불 생성 시 필요한 상태 객체.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RefundCreateState {
    private static final String PROCESSED_BY_SYSTEM = "SYSTEM";

    private final Long paymentId;
    private final Long orderId;
    private final RefundType refundType;
    private final Long refundAmount;
    private final String cancelReason;
    private final String processedBy;
    private final String pgRefundKey;
    private final String pgRawResponse;

    public static RefundCreateState withPg(
            Long paymentId,
            Long orderId,
            RefundType refundType,
            Long refundAmount,
            String cancelReason,
            String pgRefundKey,
            String pgRawResponse
    ) {
        return RefundCreateState.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .refundType(refundType)
                .refundAmount(refundAmount)
                .cancelReason(cancelReason)
                .processedBy(PROCESSED_BY_SYSTEM)
                .pgRefundKey(pgRefundKey)
                .pgRawResponse(pgRawResponse)
                .build();
    }

    public static RefundCreateState withoutPg(
            Long paymentId,
            Long orderId,
            RefundType refundType,
            Long refundAmount,
            String cancelReason
    ) {
        return RefundCreateState.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .refundType(refundType)
                .refundAmount(refundAmount)
                .cancelReason(cancelReason)
                .processedBy(PROCESSED_BY_SYSTEM)
                .build();
    }
}
