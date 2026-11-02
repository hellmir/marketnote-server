package com.personal.marketnote.commerce.domain.servicecommunication;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CommerceServiceCommunicationTargetType {
    PRODUCT_INFO("상품 정보"),
    USER_POINT("유저 포인트"),
    CART_PRODUCT("장바구니 상품");

    private final String description;
}
