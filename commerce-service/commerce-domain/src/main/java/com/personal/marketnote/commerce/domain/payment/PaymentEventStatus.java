package com.personal.marketnote.commerce.domain.payment;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentEventStatus {
    READY("결제 대기"),
    EXECUTING("결제 진행 중"),
    COMPLETE("결제 완료"),
    FAILED("결제 실패"),
    UNKNOWN("결제 상태 미확인"),
    PARTIALLY_REFUNDED("부분 환불됨"),
    REFUNDED("환불 완료"),
    CANCELLED("취소됨");

    private final String description;

    public boolean isReady() {
        return this == READY;
    }

    public boolean isExecuting() {
        return this == EXECUTING;
    }

    public boolean isComplete() {
        return this == COMPLETE;
    }

    public boolean isPartiallyRefunded() {
        return this == PARTIALLY_REFUNDED;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    public boolean isRefundable() {
        return this == COMPLETE || this == PARTIALLY_REFUNDED;
    }
}
