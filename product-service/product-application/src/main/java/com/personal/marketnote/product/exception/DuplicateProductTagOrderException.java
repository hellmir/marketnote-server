package com.personal.marketnote.product.exception;

public class DuplicateProductTagOrderException extends IllegalArgumentException {
    private static final String DUPLICATE_TAG_ID_MESSAGE = "ERR_PRODUCT_TAG_02::중복된 태그 ID가 존재합니다.";
    private static final String DUPLICATE_ORDER_NUM_MESSAGE = "ERR_PRODUCT_TAG_03::중복된 순서 번호가 존재합니다.";

    public static DuplicateProductTagOrderException duplicateTagId() {
        return new DuplicateProductTagOrderException(DUPLICATE_TAG_ID_MESSAGE);
    }

    public static DuplicateProductTagOrderException duplicateOrderNum() {
        return new DuplicateProductTagOrderException(DUPLICATE_ORDER_NUM_MESSAGE);
    }

    private DuplicateProductTagOrderException(String message) {
        super(message);
    }
}
