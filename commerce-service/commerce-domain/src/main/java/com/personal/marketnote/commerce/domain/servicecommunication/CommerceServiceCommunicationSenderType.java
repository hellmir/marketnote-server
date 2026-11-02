package com.personal.marketnote.commerce.domain.servicecommunication;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CommerceServiceCommunicationSenderType {
    USER("유저 서비스"),
    PRODUCT("상품 서비스"),
    COMMERCE("커머스 서비스"),
    COMMUNITY("커뮤니티 서비스"),
    REWARD("리워드 서비스"),
    FILE("파일 서비스"),
    FULFILLMENT("풀필먼트 서비스");

    private final String description;
}
