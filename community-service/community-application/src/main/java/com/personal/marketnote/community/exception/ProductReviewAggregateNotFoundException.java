package com.personal.marketnote.community.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class ProductReviewAggregateNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "상품 리뷰 집계 정보를 찾을 수 없습니다.";

    public ProductReviewAggregateNotFoundException() {
        super(MESSAGE);
    }
}
