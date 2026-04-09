package com.personal.marketnote.community.domain.servicecommunication;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CommunityServiceCommunicationTargetType {
    PRODUCT_INFO("상품 정보"),
    REVIEW_IMAGE("리뷰 이미지"),
    POST_IMAGE("게시글 이미지"),
    ORDER_PRODUCT_REVIEW_STATUS("주문 상품 리뷰 상태");

    private final String description;
}
