package com.personal.marketnote.commerce.domain.refund;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 환불 유형을 정의하는 열거형.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@RequiredArgsConstructor
@Getter
public enum RefundType {
    FULL_REFUND("전체 환불"),
    PARTIAL_REFUND("부분 환불");

    private final String description;
}
