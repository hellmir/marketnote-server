package com.personal.marketnote.product.exception;

public class InvalidProductPageSizeException extends IllegalArgumentException {
    private static final String MESSAGE = "ERR_PRODUCT_PAGE_SIZE_01::페이지 크기는 1 이상이어야 합니다. 전송된 페이지 크기: %d";

    public InvalidProductPageSizeException(int pageSize) {
        super(String.format(MESSAGE, pageSize));
    }
}
