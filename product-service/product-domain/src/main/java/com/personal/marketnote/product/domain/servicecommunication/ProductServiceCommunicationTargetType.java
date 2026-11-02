package com.personal.marketnote.product.domain.servicecommunication;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProductServiceCommunicationTargetType {
    PRODUCT_REVIEW_AGGREGATE("상품 리뷰 집계"),
    INVENTORY("재고"),
    PRODUCT_IMAGE("상품 이미지"),
    FULFILLMENT_AUTH("풀필먼트 인증"),
    FULFILLMENT_GOODS("풀필먼트 상품"),
    FULFILLMENT_GOODS_ELEMENT("풀필먼트 모음상품");

    private final String description;
}
