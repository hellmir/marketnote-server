package com.personal.marketnote.commerce.adapter.in.web.refund.response;

import com.personal.marketnote.commerce.domain.refund.RefundType;
import com.personal.marketnote.commerce.port.in.result.refund.GetAdminRefundResult;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 관리자 환불 조회 응답 DTO.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@Builder
public record GetAdminRefundResponse(
        Long id,
        Long paymentId,
        Long orderId,
        RefundType refundType,
        Long refundAmount,
        String cancelReason,
        String processedBy,
        String pgRefundKey,
        LocalDateTime createdAt
) {
    public static GetAdminRefundResponse from(GetAdminRefundResult result) {
        return GetAdminRefundResponse.builder()
                .id(result.id())
                .paymentId(result.paymentId())
                .orderId(result.orderId())
                .refundType(result.refundType())
                .refundAmount(result.refundAmount())
                .cancelReason(result.cancelReason())
                .processedBy(result.processedBy())
                .pgRefundKey(result.pgRefundKey())
                .createdAt(result.createdAt())
                .build();
    }
}
