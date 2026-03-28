package com.personal.marketnote.common.exception;

import java.io.IOException;

public class ProductServiceRequestFailedException extends ExternalOperationFailedException {
    private static final String PRODUCT_SERVICE_REQUEST_FAILED_EXCEPTION_MESSAGE = "상품 서비스 통신 중 오류가 발생했습니다.";

    public ProductServiceRequestFailedException(IOException cause) {
        super(String.format(PRODUCT_SERVICE_REQUEST_FAILED_EXCEPTION_MESSAGE), cause);
    }
}
