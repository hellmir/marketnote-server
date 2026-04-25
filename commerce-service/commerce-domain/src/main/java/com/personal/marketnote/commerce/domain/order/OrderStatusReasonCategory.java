package com.personal.marketnote.commerce.domain.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OrderStatusReasonCategory {
    CANCEL_ORDER("구매 의사 취소", ReasonType.CANCEL),
    CHANGE_OPTION("색상, 사이즈 등 변경", ReasonType.CANCEL),
    MISTAKE("주문 실수", ReasonType.BOTH),
    ETC("직접 입력", ReasonType.BOTH),
    SIMPLE_CHANGE_OF_MIND("단순 변심", ReasonType.RETURN),
    PRODUCT_DAMAGE("상품 파손/변질", ReasonType.RETURN),
    PRODUCT_MISMATCH("상품이 설명과 다름", ReasonType.RETURN),
    WRONG_DELIVERY("다른 상품이 배송됨", ReasonType.RETURN),
    MISSING_COMPONENTS("상품/구성품 누락", ReasonType.RETURN);

    private final String description;
    private final ReasonType reasonType;

    public boolean isCancelReason() {
        return reasonType == ReasonType.CANCEL || reasonType == ReasonType.BOTH;
    }

    public boolean isReturnReason() {
        return reasonType == ReasonType.RETURN || reasonType == ReasonType.BOTH;
    }

    public enum ReasonType {
        CANCEL, RETURN, BOTH
    }
}
