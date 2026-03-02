package com.personal.marketnote.commerce.port.in.result.refund;

import com.personal.marketnote.commerce.domain.refund.Refund;
import com.personal.marketnote.commerce.domain.refund.RefundType;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 관리자 환불 조회 결과 레코드.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@Builder
public record GetAdminRefundResult(
        Long id,
        Long paymentId,
        Long orderId,
        RefundType refundType,
        Long refundAmount,
        String cancelReason,
        String processedBy,
        String pgRefundKey,
        String pgRawResponse,
        LocalDateTime createdAt
) {
    /**
     * 환불 도메인 객체로부터 조회 결과를 생성한다.
     *
     * @param refund 환불 도메인 객체
     * @return 관리자 환불 조회 결과
     */
    public static GetAdminRefundResult from(Refund refund) {
        return GetAdminRefundResult.builder()
                .id(refund.getId())
                .paymentId(refund.getPaymentId())
                .orderId(refund.getOrderId())
                .refundType(refund.getRefundType())
                .refundAmount(refund.getRefundAmount())
                .cancelReason(refund.getCancelReason())
                .processedBy(refund.getProcessedBy())
                .pgRefundKey(refund.getPgRefundKey())
                .pgRawResponse(refund.getPgRawResponse())
                .createdAt(refund.getCreatedAt())
                .build();
    }
}
