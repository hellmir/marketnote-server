package com.personal.marketnote.product.exception;

public class CategoryHasChildrenException extends IllegalStateException {
    private static final String MESSAGE = "%s::하위 카테고리가 존재합니다. 전송된 카테고리 ID: %d";

    public CategoryHasChildrenException(String code, Long categoryId) {
        super(String.format(MESSAGE, code, categoryId));
    }
}
