package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class PricePolicyNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "가격 정책을 찾을 수 없습니다.";

    public PricePolicyNotFoundException() {
        super(MESSAGE);
    }
}
