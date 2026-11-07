package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.BusinessSecurityException;

public class NotProductOwnerException extends BusinessSecurityException {
    private static final String MESSAGE = "ERR_PRODUCT_OWNER_01::관리자 또는 상품 판매자가 아닙니다.";

    public NotProductOwnerException() {
        super(MESSAGE);
    }
}
