package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.BusinessSecurityException;

public class InvalidProductOptionOwnershipException extends BusinessSecurityException {
    private static final String MESSAGE = "ERR_PRODUCT_OPTION_01::상품에 속하지 않은 옵션이 포함되어 있습니다.";

    public InvalidProductOptionOwnershipException() {
        super(MESSAGE);
    }
}
