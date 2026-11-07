package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class ProductTagNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "상품 태그를 찾을 수 없습니다.";

    public ProductTagNotFoundException() {
        super(MESSAGE);
    }
}
