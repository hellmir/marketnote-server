package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsWithMessageException;

public class DuplicateFasstoProductCodeException extends DomainAlreadyExistsWithMessageException {

    public DuplicateFasstoProductCodeException(String fasstoProductCode) {
        super("ERR_PRODUCT_FASSTO_01::이미 등록된 fasstoProductCode입니다. fasstoProductCode=" + fasstoProductCode);
    }
}
