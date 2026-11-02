package com.personal.marketnote.product.exception;

public class CategoryHasProductsException extends IllegalStateException {
    private static final String MESSAGE = "%s::해당 카테고리에 등록된 상품이 존재합니다. 전송된 카테고리 ID: %d";

    public CategoryHasProductsException(String code, Long categoryId) {
        super(String.format(MESSAGE, code, categoryId));
    }
}
