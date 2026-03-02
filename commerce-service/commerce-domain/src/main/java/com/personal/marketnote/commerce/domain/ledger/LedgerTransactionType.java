package com.personal.marketnote.commerce.domain.ledger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LedgerTransactionType {
    PAYMENT_APPROVAL("결제 승인"),
    PAYMENT_CANCELLATION("결제 전체 취소"),
    PAYMENT_PARTIAL_REFUND("결제 부분 환불"),
    PG_SETTLEMENT("PG 정산 입금"),
    SELLER_SETTLEMENT("판매자 정산"),
    SETTLEMENT_CANCELLATION("정산 취소 역분개");

    private final String description;
}
