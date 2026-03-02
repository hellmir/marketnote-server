package com.personal.marketnote.commerce.domain.refund;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 환불 영속화 상태 복원 객체.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RefundSnapshotState {
    private final Long id;
    private final Long paymentId;
    private final Long orderId;
    private final RefundType refundType;
    private final Long refundAmount;
    private final String cancelReason;
    private final String processedBy;
    private final String pgRefundKey;
    private final String pgRawResponse;
    private final LocalDateTime createdAt;
}
