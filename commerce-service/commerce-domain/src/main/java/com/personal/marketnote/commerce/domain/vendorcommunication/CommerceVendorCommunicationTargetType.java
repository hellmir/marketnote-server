package com.personal.marketnote.commerce.domain.vendorcommunication;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CommerceVendorCommunicationTargetType {
    TRADE_REGISTER("거래등록"),
    PAYMENT_APPROVAL("결제승인"),
    PAYMENT_CANCEL("결제취소"),
    QUICK_PAYMENT_TRADE_REGISTER("빠른결제 거래등록");

    private final String description;
}
