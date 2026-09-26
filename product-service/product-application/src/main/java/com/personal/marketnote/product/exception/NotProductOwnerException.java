package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.BusinessSecurityException;

public class NotProductOwnerException extends BusinessSecurityException {
    private static final String NOT_PRODUCT_OWNER_EXCEPTION_MESSAGE = "%s:: 관리자 또는 상품 판매자가 아닙니다. 전송된 상품 ID: %d";

    public NotProductOwnerException(String code, Long productId) {
        super(String.format(NOT_PRODUCT_OWNER_EXCEPTION_MESSAGE, code, productId));
    }
}

