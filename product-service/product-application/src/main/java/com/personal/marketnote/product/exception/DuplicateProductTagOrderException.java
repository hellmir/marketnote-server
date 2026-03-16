package com.personal.marketnote.product.exception;

import lombok.Getter;

@Getter
public class DuplicateProductTagOrderException extends IllegalArgumentException {
    private static final String DUPLICATE_TAG_ID_MESSAGE =
            "ERR_PRODUCT_TAG_02::중복된 태그 ID가 존재합니다. tagId=%d";
    private static final String DUPLICATE_ORDER_NUM_MESSAGE =
            "ERR_PRODUCT_TAG_03::중복된 순서 번호가 존재합니다. orderNum=%d";

    public static DuplicateProductTagOrderException duplicateTagId(Long tagId) {
        return new DuplicateProductTagOrderException(String.format(DUPLICATE_TAG_ID_MESSAGE, tagId));
    }

    public static DuplicateProductTagOrderException duplicateOrderNum(Long orderNum) {
        return new DuplicateProductTagOrderException(String.format(DUPLICATE_ORDER_NUM_MESSAGE, orderNum));
    }

    private DuplicateProductTagOrderException(String message) {
        super(message);
    }
}
