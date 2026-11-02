package com.personal.marketnote.community.domain.post;

import com.personal.marketnote.common.utility.FormatConverter;
import lombok.Getter;

@Getter
public enum PostFilterValue {
    // 상품 문의 필터
    TRUE("참"),

    // FAQ 카테고리
    ORDER_PAYMENT("FAQ-주문/결제"),
    DELIVERY("FAQ-배송 관련"),
    CANCEL_REFUND("FAQ-취소/환불"),
    RETURN_EXCHANGE("FAQ-반품/교환"),
    POINT("FAQ-적립금"),
    EVENT_COUPON("FAQ-이벤트/쿠폰"),
    LOGIN_MEMBER_INFO("FAQ-로그인/회원정보");

    private final String description;
    private final String camelCaseValue;

    PostFilterValue(String description) {
        this.description = description;
        camelCaseValue = FormatConverter.snakeToCamel(name());
    }

    public boolean isTrue() {
        return this == TRUE;
    }
}
