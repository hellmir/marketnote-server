package com.personal.marketnote.product.exception;

public class CategoryHasChildrenException extends IllegalStateException {
    private static final String MESSAGE = "ERR_CATEGORY_CHILDREN_01::하위 카테고리가 존재합니다.";

    public CategoryHasChildrenException() {
        super(MESSAGE);
    }
}
