package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class ProductNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "상품을 찾을 수 없습니다.";

    public ProductNotFoundException() {
        super(MESSAGE);
    }
}
