package com.personal.marketnote.community.domain.post;

import com.personal.marketnote.common.utility.FormatConverter;
import lombok.Getter;

@Getter
public enum PostFilterValue {
    // 상품 문의 필터
    TRUE("참"),

    // FAQ 카테고리
    ORDER_DELIVERY("FAQ-주문/배송"),
    CANCEL_RETURN("FAQ-취소/반품"),
    BENEFIT_REWARD("FAQ-혜택/리워드"),
    PRODUCT("FAQ-제품"),
    ACCOUNT("FAQ-계정");

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
