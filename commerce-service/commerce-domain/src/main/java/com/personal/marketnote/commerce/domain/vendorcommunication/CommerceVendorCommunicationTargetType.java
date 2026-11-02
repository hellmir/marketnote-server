package com.personal.marketnote.commerce.domain.vendorcommunication;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CommerceVendorCommunicationTargetType {
    TRADE_REGISTER("거래등록"),
    PAYMENT_APPROVAL("결제승인"),
    PAYMENT_CANCEL("결제취소"),
    QUICK_PAYMENT_TRADE_REGISTER("빠른결제 거래등록"),
    QUICK_PAYMENT_BATCH_KEY_ISSUANCE("빠른결제 배치키 발급"),
    QUICK_PAYMENT_BATCH_APPROVAL("빠른결제 배치 결제승인"),
    QUICK_PAYMENT_BATCH_KEY_DELETION("빠른결제 배치키 삭제");

    private final String description;
}
