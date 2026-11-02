package com.personal.marketnote.community.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import lombok.Getter;

@Getter
public class ProductReviewAggregateNotFoundException extends DomainNotFoundException {
    private static final String PRODUCT_REVIEW_AGGREGATE_NOT_FOUND_EXCEPTION_MESSAGE = "상품 리뷰 집계 정보를 찾을 수 없습니다. 전송된 상품 ID: %d";

    public ProductReviewAggregateNotFoundException(Long productId) {
        super(String.format(PRODUCT_REVIEW_AGGREGATE_NOT_FOUND_EXCEPTION_MESSAGE, productId));
    }
}
