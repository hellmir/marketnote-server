package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class CategoryNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "존재하지 않는 카테고리입니다.";

    public CategoryNotFoundException() {
        super(MESSAGE);
    }
}
