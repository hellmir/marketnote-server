package com.personal.marketnote.commerce.domain.order;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderStatusReasonCategory {
    CANCEL_ORDER("구매 의사 취소"),
    CHANGE_OPTION("색상, 사이즈 등 변경"),
    MISTAKE("주문 실수"),
    ETC("직접 입력");

    private final String description;
}
