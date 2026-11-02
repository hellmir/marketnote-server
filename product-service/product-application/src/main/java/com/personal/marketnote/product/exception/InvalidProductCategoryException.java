package com.personal.marketnote.product.exception;

public class InvalidProductCategoryException extends IllegalArgumentException {
    private static final String MESSAGE = "ERR_PRODUCT_CATEGORY_01::비활성 상태이거나 존재하지 않는 카테고리 ID가 포함되어 있습니다.";

    public InvalidProductCategoryException() {
        super(MESSAGE);
    }
}
